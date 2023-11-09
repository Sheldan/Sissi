package dev.sheldan.sissi.module.debra.api;

import dev.sheldan.sissi.module.debra.model.api.*;
import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import dev.sheldan.sissi.module.debra.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME;

@RestController
@RequestMapping(value = "/debra")
public class DebraDonationStatusController {

    @Autowired
    private DonationService donationService;

    @GetMapping(value = "/latestDonations", produces = "application/json")
    public DonationStats getLatestDonations() {
        Long serverId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        DonationsResponse donationResponse = donationService.getCachedDonationAmount(serverId);
        List<DonationInfo> donations = donationService.getLatestDonations(donationResponse, Integer.MAX_VALUE)
                .stream()
                .map(DonationInfo::fromDonationItemModel)
                .toList();
        return DonationStats
                .builder()
                .totalAmount(donationResponse.getPage().getCollected())
                .donations(donations)
                .build();
    }

    @GetMapping(value = "/highestDonations", produces = "application/json")
    public DonationStats getHighestDonations() {
        Long serverId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        DonationsResponse donationResponse = donationService.getCachedDonationAmount(serverId);
        List<DonationInfo> donations = donationService.getHighestDonations(donationResponse, Integer.MAX_VALUE)
                .stream()
                .map(DonationInfo::fromDonationItemModel)
                .toList();
        return DonationStats
                .builder()
                .totalAmount(donationResponse.getPage().getCollected())
                .donations(donations)
                .build();
    }

    @GetMapping(value = "/campaignInfo", produces = "application/json")
    public CampaignInfo getCampaignInfo() {
        Long serverId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
        DonationsResponse donationResponse = donationService.getCachedDonationAmount(serverId);

        Description pageObject = donationResponse.getPage();
        return CampaignInfo
                .builder()
                .collected(pageObject.getCollected())
                .collectedNet(pageObject.getCollectedNet())
                .donationCount(donationResponse.getDonationCount())
                .currency(pageObject.getCurrency())
                .percent(pageObject.getPercent())
                .displayName(pageObject.getDisplayName())
                .slug(pageObject.getSlug())
                .target(pageObject.getTarget())
                .build();
    }
}
