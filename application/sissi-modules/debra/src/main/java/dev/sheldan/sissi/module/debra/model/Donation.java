package dev.sheldan.sissi.module.debra.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class Donation {
    private String donatorName;
    private BigDecimal amount;
    private String message;
}
