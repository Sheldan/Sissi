package dev.sheldan.sissi.module.debra.model.api;

import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DonationInfo {
    private String name;
    private BigDecimal donationAmount;
    private Boolean anonymous;

    public static DonationInfo fromDonationItemModel(DonationItemModel donationItemModel) {
        return DonationInfo
                .builder()
                .donationAmount(donationItemModel.getDonationAmount())
                .anonymous(donationItemModel.getAnonymous())
                .name(donationItemModel.getName())
                .build();
    }
}
