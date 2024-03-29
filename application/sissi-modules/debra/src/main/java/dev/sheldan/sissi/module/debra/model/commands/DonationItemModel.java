package dev.sheldan.sissi.module.debra.model.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DonationItemModel {
    private String firstName;
    private String lastName;
    private BigDecimal donationAmount;
    private Boolean anonymous;
}
