package dev.sheldan.sissi.module.debra.api;

import dev.sheldan.sissi.module.debra.model.api.*;
import dev.sheldan.sissi.module.debra.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping(value = "/debra")
public class DebraDonationStatusController {

    @Autowired
    private DonationService donationService;

    @GetMapping(value = "/latestDonations", produces = "application/json")
    public DonationStats getLatestDonations() {
        DonationsResponse donationResponse = donationService.getSynchronizedCachedDonationAmount();
        List<DonationInfo> donations = donationService.getLatestDonations(donationResponse, Integer.MAX_VALUE)
                .stream()
                .map(DonationInfo::fromDonationItemModel)
                .toList();
        return DonationStats
                .builder()
                .totalAmount(donationResponse.getCurrentDonationAmount())
                .donations(donations)
                .build();
    }

    @GetMapping(value = "/highestDonations", produces = "application/json")
    public DonationStats getHighestDonations() {
        DonationsResponse donationResponse = donationService.getSynchronizedCachedDonationAmount();
        List<DonationInfo> donations = donationService.getHighestDonations(donationResponse, Integer.MAX_VALUE)
                .stream()
                .map(DonationInfo::fromDonationItemModel)
                .toList();
        return DonationStats
                .builder()
                .totalAmount(donationResponse.getCurrentDonationAmount())
                .donations(donations)
                .build();
    }

    @GetMapping(value = "/campaignInfo", produces = "application/json")
    public CampaignInfo getCampaignInfo() {
        DonationsResponse donationResponse = donationService.getSynchronizedCachedDonationAmount();

        return CampaignInfo
                .builder()
                .donationCount(donationResponse.getDonationCount())
                .collected(donationResponse.getCurrentDonationAmount())
                .target(donationResponse.getDonationAmountGoal())
                .percent(donationResponse.getCurrentDonationAmount().divide(donationResponse.getDonationAmountGoal(), RoundingMode.CEILING))
                .currency("€")
                .build();
    }
}
