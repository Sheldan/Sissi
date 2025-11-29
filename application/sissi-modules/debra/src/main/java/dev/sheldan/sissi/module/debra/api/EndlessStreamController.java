package dev.sheldan.sissi.module.debra.api;

import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.sissi.module.debra.config.DebraFeatureConfig;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.api.EndlessStreamInfo;
import dev.sheldan.sissi.module.debra.model.database.EndlessStream;
import dev.sheldan.sissi.module.debra.service.DonationService;
import dev.sheldan.sissi.module.debra.service.management.EndlessStreamManagementServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME;

@RestController
@RequestMapping(value = "/stream")
public class EndlessStreamController {

    @Autowired
    private EndlessStreamManagementServiceBean endlessStreamManagementServiceBean;

    @Autowired
    private DonationService donationService;

    @Autowired
    private ConfigService configService;

    @GetMapping(value = "/endlessStream/{id}", produces = "application/json")
    public EndlessStreamInfo getLatestDonations(@PathVariable("id") Long id) {
        Long serverId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        EndlessStream endlessStream = endlessStreamManagementServiceBean.getEndlessStream(id);
        DonationsResponse donationInfo = donationService.getSynchronizedCachedDonationAmount();
        BigDecimal collectedAmount = donationInfo.getCurrentDonationAmount();
        Long minuteRate = configService.getLongValueOrConfigDefault(DebraFeatureConfig.ENDLESS_STREAM_MINUTE_RATE, serverId);
        Instant endDate = endlessStream.getStartTime().plus(collectedAmount.multiply(new BigDecimal(minuteRate)).toBigInteger().longValue(), ChronoUnit.MINUTES);
        return EndlessStreamInfo
                .builder()
                .startDate(endlessStream.getStartTime())
                .endDate(endDate)
                .donationAmount(collectedAmount.longValue())
                .minuteRate(minuteRate)
                .build();
    }
}
