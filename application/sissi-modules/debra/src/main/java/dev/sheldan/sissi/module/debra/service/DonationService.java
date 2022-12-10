package dev.sheldan.sissi.module.debra.service;

import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.sissi.module.debra.DonationAmountNotFoundException;
import dev.sheldan.sissi.module.debra.config.DebraPostTarget;
import dev.sheldan.sissi.module.debra.config.DebraProperties;
import dev.sheldan.sissi.module.debra.model.Donation;
import dev.sheldan.sissi.module.debra.model.listener.DonationNotificationModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY = "debra_donation_notification";

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("(.*) hat (\\d{1,9},\\d{2}) Euro gespendet!<br \\/>Vielen Dank!<br \\/>Nachricht:<br \\/>(.*)");
    private static final Pattern DONATION_PAGE_AMOUNT_PARTNER = Pattern.compile("\"metric4\",\\s*\"(.*)\"");

    public Donation parseDonationFromMessage(String message) {
        Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (matcher.find()) {
            String donatorName = matcher.group(1);
            String amountString = matcher.group(2);
            BigDecimal amount = new BigDecimal(amountString.replace(',', '.'));
            String donationMessage = Optional.ofNullable(matcher.group(3)).map(msg -> msg.replaceAll("(<br>)+", " ")).map(String::trim).orElse("");
            return Donation
                    .builder()
                    .message(donationMessage)
                    .donatorName(donatorName)
                    .amount(amount)
                    .build();
        } else {
            throw new IllegalArgumentException("String in wrong format");
        }
    }

    public BigDecimal fetchCurrentDonationAmount() {
        try (InputStream is = new URL(debraProperties.getDonationsPageURL()).openStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = DONATION_PAGE_AMOUNT_PARTNER.matcher(line);
                if (matcher.find()) {
                    return new BigDecimal(matcher.group(1).replace(',', '.'));
                }
            }
            log.warn("Did not find the donation amount in the configured URL {}", debraProperties.getDonationsPageURL());
            throw new DonationAmountNotFoundException();
        } catch (IOException ex) {
            log.warn("Failed to load page for parsing donation amount {}.", debraProperties.getDonationsPageURL(), ex);
            throw new DonationAmountNotFoundException();
        }
    }

    public CompletableFuture<Void> sendDonationNotification(Donation donation) {
        DonationNotificationModel model = DonationNotificationModel
                .builder()
                .donation(donation)
                .totalDonationAmount(fetchCurrentDonationAmount())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(DEBRA_DONATION_NOTIFICATION_TEMPLATE_KEY, model);
        Long targetServerId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        List<CompletableFuture<Message>> firstMessage = postTargetService.sendEmbedInPostTarget(messageToSend, DebraPostTarget.DEBRA_DONATION_NOTIFICATION, targetServerId);
        List<CompletableFuture<Message>> secondMessage = postTargetService.sendEmbedInPostTarget(messageToSend, DebraPostTarget.DEBRA_DONATION_NOTIFICATION2, targetServerId);
        firstMessage.addAll(secondMessage);
        return FutureUtils.toSingleFutureGeneric(firstMessage);
    }
}
