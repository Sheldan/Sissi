package dev.sheldan.sissi.module.debra.model.listener;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationNotificationModel {
    private DonationResponseModel donation;
    private BigDecimal totalDonationAmount;
}
