package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class CampaignInfo {
    private Integer donationCount;
    private BigDecimal collected;
    private BigDecimal target;
    private String currency;
    private BigDecimal percent;
}
