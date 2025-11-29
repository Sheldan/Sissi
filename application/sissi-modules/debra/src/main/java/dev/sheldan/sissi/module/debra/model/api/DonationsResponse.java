package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class DonationsResponse {
    private BigDecimal currentDonationAmount;
    private BigDecimal donationAmountGoal;
    private int donationCount;
    private List<DonationDto> donations;
}
