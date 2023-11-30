package dev.sheldan.sissi.module.debra.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.DonationAmountNotFoundException;
import dev.sheldan.sissi.module.debra.config.DebraPostTarget;
import dev.sheldan.sissi.module.debra.config.DebraProperties;
import dev.sheldan.sissi.module.debra.converter.DonationConverter;
import dev.sheldan.sissi.module.debra.model.api.Donation;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.commands.DebraInfoButtonPayload;
import dev.sheldan.sissi.module.debra.model.commands.DebraInfoModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import dev.sheldan.sissi.module.debra.model.listener.DonationResponseModel;
import dev.sheldan.sissi.module.debra.model.listener.DonationNotificationModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_API_FETCH_SIZE_KEY;
import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME;

@Component
@Slf4j
public class DonationService {

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private DebraProperties debraProperties;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private DonationConverter donationConverter;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DonationService self;

    private static final String DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY = "debra_donation_notification";

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("(.*) hat (\\d{1,9},\\d{2}) Euro gespendet!<br \\/>Vielen Dank!<br \\/>Nachricht:<br \\/>(.*)");

    private static final String DEBRA_INFO_BUTTON_MESSAGE_TEMPLATE_KEY = "debraInfoButton";
    public static final String DEBRA_INFO_BUTTON_ORIGIN = "DEBRA_INFO_BUTTON";

    public DonationResponseModel parseDonationFromMessage(String message) {
        Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (matcher.find()) {
            String donatorName = matcher.group(1);
            String amountString = matcher.group(2);
            BigDecimal amount = new BigDecimal(amountString.replace(',', '.'));
            String donationMessage = Optional.ofNullable(matcher.group(3)).map(msg -> msg.replaceAll("(<br>)+", " ")).map(String::trim).orElse("");
            return DonationResponseModel
                    .builder()
                    .message(donationMessage)
                    .donatorName(donatorName)
                    .amount(amount)
                    .build();
        } else {
            throw new IllegalArgumentException("String in wrong format");
        }
    }

    public List<DonationItemModel> getHighestDonations(DonationsResponse response, Integer maxCount) {
        List<Donation> topDonations = response
                .getDonations()
                .stream()
                .sorted(Comparator.comparing(Donation::getAmount)
                        .reversed())
                .collect(Collectors.toList());
        return topDonations
                .stream()
                .limit(maxCount)
                .map(donation -> donationConverter.convertDonation(donation))
                .collect(Collectors.toList());
    }

    public List<DonationItemModel> getLatestDonations(DonationsResponse response, Integer maxCount) {
        return response
                .getDonations()
                .stream()
                .limit(maxCount)
                .map(donation -> donationConverter.convertDonation(donation))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "donation-cache")
    public DonationsResponse getCachedDonationAmount(Long serverId) {
        return fetchCurrentDonationAmount(serverId);
    }

    public DonationsResponse fetchCurrentDonationAmount(Long serverId) {
        try {
            Long fetchSize = configService.getLongValueOrConfigDefault(DEBRA_DONATION_API_FETCH_SIZE_KEY, serverId);
            Request request = new Request.Builder()
                    .url(String.format(debraProperties.getDonationAPIUrl(), fetchSize))
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if(!response.isSuccessful()) {
                log.error("Failed to retrieve donation response. Response had code {} with body {} and headers {}.",
                        response.code(), response.body().string(), response.headers());
                throw new DonationAmountNotFoundException();
            }
            Gson gson = getGson();
            return gson.fromJson(response.body().string(), DonationsResponse.class);
        } catch (Exception exception) {
            throw new AbstractoRunTimeException(exception);
        }

    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(BigDecimal.class, new BigDecimalGsonAdapter())
                .create();
    }

    private DonationsModel getDonationInfoModel(Long serverId) {
        return donationConverter.convertDonationResponse(fetchCurrentDonationAmount(serverId));
    }

    public CompletableFuture<Void> sendDonationNotification(DonationResponseModel donation) throws IOException {
        Long targetServerId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        DonationsModel donationInfoModel = getDonationInfoModel(targetServerId);
        DonationNotificationModel model = DonationNotificationModel
                .builder()
                .donation(donation)
                .totalDonationAmount(donationInfoModel.getTotalAmount())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY, model);
        List<CompletableFuture<Message>> firstMessage = postTargetService.sendEmbedInPostTarget(messageToSend, DebraPostTarget.DEBRA_DONATION_NOTIFICATION, targetServerId);
        List<CompletableFuture<Message>> secondMessage = postTargetService.sendEmbedInPostTarget(messageToSend, DebraPostTarget.DEBRA_DONATION_NOTIFICATION2, targetServerId);
        firstMessage.addAll(secondMessage);
        return FutureUtils.toSingleFutureGeneric(firstMessage);
    }

    public CompletableFuture<Void> sendDebraInfoButtonMessage(GuildMessageChannel guildMessageChannel) {
        String buttonId = componentService.generateComponentId();
        DebraInfoModel model = DebraInfoModel
                .builder()
                .buttonId(buttonId)
                .build();

        MessageToSend messageToSend = templateService.renderEmbedTemplate(DEBRA_INFO_BUTTON_MESSAGE_TEMPLATE_KEY, model, guildMessageChannel.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, guildMessageChannel)).thenAccept(unused -> {
            self.persistButtonPayload(guildMessageChannel, buttonId);
        });
    }

    @Transactional
    public void persistButtonPayload(GuildMessageChannel guildMessageChannel, String buttonId) {
        DebraInfoButtonPayload payload = DebraInfoButtonPayload
                .builder()
                .build();
        AServer server = serverManagementService.loadServer(guildMessageChannel.getGuild().getIdLong());
        componentPayloadService.createButtonPayload(buttonId, payload, DEBRA_INFO_BUTTON_ORIGIN, server);
    }
}
