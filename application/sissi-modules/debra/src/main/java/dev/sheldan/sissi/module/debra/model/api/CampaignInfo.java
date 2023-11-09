package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Builder
@Getter
public class CampaignInfo {
    private BigInteger donationCount;
    private BigDecimal collected;
    private BigDecimal target;
    private String currency;
    private String slug;
    private String displayName;
    private BigDecimal collectedNet;
    private BigDecimal percent;
}
