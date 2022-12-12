package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class Description {
    private BigDecimal collected;
    private BigDecimal target;
    private String currency;
    private String slug;
    private String displayName;
    private BigDecimal collectedNet;
    private BigDecimal percent;
}
