package dev.sheldan.sissi.module.debra.model.commands;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationsModel {
    private BigDecimal donationAmount;
}
