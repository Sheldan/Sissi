package dev.sheldan.sissi.module.debra.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.HashService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.config.DebraPostTarget;
import dev.sheldan.sissi.module.debra.config.DebraProperties;
import dev.sheldan.sissi.module.debra.converter.DonationConverter;
import dev.sheldan.sissi.module.debra.model.api.DonationDto;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.commands.DebraInfoButtonPayload;
import dev.sheldan.sissi.module.debra.model.commands.DebraInfoModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import dev.sheldan.sissi.module.debra.model.database.Donation;
import dev.sheldan.sissi.module.debra.model.listener.DonationResponseModel;
import dev.sheldan.sissi.module.debra.model.listener.DonationNotificationModel;
import dev.sheldan.sissi.module.debra.service.management.DonationManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private DonationConverter donationConverter;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DonationManagementServiceBean donationManagementServiceBean;

    @Autowired
    private HashService hashService;

    @Autowired
    private DonationService self;

    private static final String DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY = "debra_donation_notification";
    private static final String DEBRA_DONATION_PING_NOTIFICATION_TEMPLATE_KEY = "debra_donation_notification_ping_notification";

    private static final String DEBRA_INFO_BUTTON_MESSAGE_TEMPLATE_KEY = "debraInfoButton";
    public static final String DEBRA_INFO_BUTTON_ORIGIN = "DEBRA_INFO_BUTTON";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d.M.y");

    public List<DonationItemModel> getHighestDonations(DonationsResponse response, Integer maxCount) {
        return response
                .getDonations()
                .stream()
                .sorted(Comparator.comparing(DonationDto::getAmount)
                        .reversed())
                .limit(maxCount)
                .map(donation -> donationConverter.convertDonation(donation))
                .toList();
    }

    public List<DonationItemModel> getLatestDonations(DonationsResponse response, Integer maxCount) {
        return response
                .getDonations()
                .stream()
                .sorted(Comparator.comparing(DonationDto::getDate).reversed())
                .limit(maxCount)
                .map(donation -> donationConverter.convertDonation(donation))
                .collect(Collectors.toList());
    }

    public synchronized DonationsResponse getSynchronizedCachedDonationAmount() {
        return self.getCachedDonationAmount();
    }

    @Cacheable(value = "donation-cache")
    public synchronized DonationsResponse getCachedDonationAmount() {
        return self.fetchCurrentDonations();
    }

    public DonationsResponse fetchCurrentDonations() {
        try {
            Document donationPage = Jsoup.connect(debraProperties.getDonationPageUrl()).get();
            DecimalFormat decimalFormat = getDecimalFormat();
            Element endValueElement = donationPage.getElementById("end-value");
            String endValueString = endValueElement.text();
            Elements currentValueElement = donationPage.getElementsByClass("current_amount").get(0).getElementsByClass("value");
            String[] valueArray = currentValueElement.text().split(" ");
            String currentValueString = valueArray[0];
            String currency = valueArray[1];
            BigDecimal currentValue = (BigDecimal) decimalFormat.parse(currentValueString);
            BigDecimal endValue = (BigDecimal) decimalFormat.parse(endValueString);
            Element list = donationPage.getElementsByClass("donor-list").first();
            Elements donationElements = list.getElementsByClass("list-item");
            List<DonationDto> donations = new ArrayList<>();
            for (Element donationMainElement : donationElements.asList()) {
                Elements nameElement = donationMainElement.getElementsByClass("donor-list-name");
                Elements dateElement = donationMainElement.getElementsByClass("donor-list-date");
                Elements amountElement = donationMainElement.getElementsByClass("donor-list-amount");
                Elements textElement = donationMainElement.getElementsByClass("donor-list-amount-text");
                LocalDate dateValue;
                if (dateElement.hasText()) {
                    dateValue = LocalDate.parse(dateElement.text(), DATE_FORMAT);
                } else {
                    dateValue = null;
                }
                BigDecimal amount;
                if (amountElement.hasText()) {
                    String amountText = amountElement.text().split(" ")[0];
                    amount = (BigDecimal) decimalFormat.parse(amountText);
                } else {
                    amount = null;
                }
                String additionalText = textElement.text();
                String name = nameElement.text();
                boolean anonymous = name.isBlank();
                donations.add(DonationDto
                        .builder()
                        .anonymous(anonymous)
                        .name(nameElement.text())
                        .amount(amount)
                        .currency(currency)
                        .name(name)
                        .text(additionalText)
                        .date(dateValue)
                        .build());
            }
            return DonationsResponse
                    .builder()
                    .donations(donations)
                    .currentDonationAmount(currentValue)
                    .donationAmountGoal(endValue)
                    .donationCount(donations.size())
                    .build();
        } catch (Exception exception) {
            throw new AbstractoRunTimeException(exception);
        }

    }

    private DecimalFormat getDecimalFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        String pattern = "#,##0.0#";

        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }

    private DonationsModel getDonationInfoModel() {
        return donationConverter.convertDonationResponse(fetchCurrentDonations());
    }


    private String hashDonation(DonationDto donation) {
        return hashService.sha256HashString(donation.stringRepresentation());
    }

    @Transactional
    public void checkForNewDonations() {
        List<Donation> allDonations = donationManagementServiceBean.getAllDonations();
        Map<String, Integer> existingHashes = allDonations
                .stream()
                .collect(Collectors.toMap(Donation::getId, Donation::getCount));
        DonationsResponse donationResponse = fetchCurrentDonations();
        Map<String, Pair<Integer, DonationDto>> donationFromPageHashes = new HashMap<>();
        donationResponse.getDonations().forEach(donationDto -> {
            String thisHash = hashDonation(donationDto);
            if(donationFromPageHashes.containsKey(thisHash)) {
                donationFromPageHashes.put(thisHash, Pair.of(donationFromPageHashes.get(thisHash).getLeft() + 1, donationDto));
            } else {
                donationFromPageHashes.put(thisHash, Pair.of(1, donationDto));
            }
        });

        Set<String> pageHashesToRemove = new HashSet<>();
        donationFromPageHashes.entrySet().forEach(pageHash -> {
            if(existingHashes.containsKey(pageHash.getKey())) {
                Integer existingDonation = existingHashes.get(pageHash.getKey());
                int amountDifference = pageHash.getValue().getKey() - existingDonation;
                if(amountDifference == 0) {
                    pageHashesToRemove.add(pageHash.getKey()); // it matches 1:1, we know about all of them already
                } if(amountDifference < 0) {
                    pageHashesToRemove.add(pageHash.getKey());
                    log.warn("We have more donations than on the page of hash {}:{}.", pageHash.getKey(), amountDifference);
                } else {
                    pageHash.setValue(Pair.of(amountDifference, pageHash.getValue().getRight()));
                }
            }
        });
        pageHashesToRemove.forEach(donationFromPageHashes::remove);
        if(donationFromPageHashes.isEmpty()) {
            log.info("No new donations - ending search.");
            return;
        }

        List<CompletableFuture<Void>> notificationFutures = new ArrayList<>();
        donationFromPageHashes.values().forEach(donationInfo -> {
            for (int i = 0; i < donationInfo.getLeft(); i++) {
                DonationDto donationDto = donationInfo.getRight();
                DonationResponseModel model = DonationResponseModel
                        .builder()
                        .message(donationDto.getText())
                        .donatorName(donationDto.getName())
                        .amount(donationDto.getAmount())
                        .anonymous(donationDto.getAnonymous())
                        .build();
                notificationFutures.add(sendDonationNotification(model));
            }
        });
        new CompletableFutureList<>(notificationFutures).getMainFuture().thenAccept(unused -> {
            log.info("All {} notifications send.", notificationFutures.size());
        }).exceptionally(throwable -> {
            log.warn("Failed to send notifications about {} new donations.", notificationFutures.size(), throwable);
            return null;
        });
        log.info("Creating/updating {} donation entries.", donationFromPageHashes.size());
        allDonations.forEach(donation -> {
            Set<String> donationsToRemoveBecauseUpdate = new HashSet<>();
            donationFromPageHashes.forEach((key, value) -> {
                // its assumed that donationFromPageHashes only contains donations that need to be created
                if (donation.getId().equals(key)) {
                    donation.setCount(value.getLeft() + donation.getCount());
                    donationManagementServiceBean.updateDonation(donation);
                    donationsToRemoveBecauseUpdate.add(key);
                }
            });
            donationsToRemoveBecauseUpdate.forEach(donationFromPageHashes::remove);
        });
        donationFromPageHashes.forEach((key, value) ->
                donationManagementServiceBean.saveDonation(key, value.getLeft()));
    }

    public CompletableFuture<Void> sendDonationNotification(DonationResponseModel donation) {
        Long targetServerId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        DonationsModel donationInfoModel = getDonationInfoModel();
        DonationNotificationModel model = DonationNotificationModel
                .builder()
                .donation(donation)
                .totalDonationAmount(donationInfoModel.getTotalAmount())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY, model, targetServerId);
        MessageToSend pingMessageToSend = templateService.renderEmbedTemplate(DEBRA_DONATION_PING_NOTIFICATION_TEMPLATE_KEY, model, targetServerId);
        return FutureUtils.toSingleFutureGenericList(postTargetService.sendEmbedInPostTarget(List.of(pingMessageToSend, messageToSend),
                DebraPostTarget.DEBRA_DONATION_NOTIFICATION, targetServerId));
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
