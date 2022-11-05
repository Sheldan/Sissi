package dev.sheldan.sissi.module.debra.model.listener;

import dev.sheldan.sissi.module.debra.model.Donation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationNotificationModel {
    private Donation donation;
    private BigDecimal totalDonationAmount;
}
