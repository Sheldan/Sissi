package dev.sheldan.sissi.module.debra.model.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DonationItemModel {
    private String name;
    private LocalDate date;
    private BigDecimal donationAmount;
    private Boolean anonymous;
}
