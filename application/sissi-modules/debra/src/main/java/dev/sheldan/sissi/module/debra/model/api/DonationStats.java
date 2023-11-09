package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DonationStats {
    private List<DonationInfo> donations;
    private BigDecimal totalAmount;
}
