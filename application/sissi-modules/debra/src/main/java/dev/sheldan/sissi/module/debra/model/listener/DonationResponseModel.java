package dev.sheldan.sissi.module.debra.model.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class DonationResponseModel {
    private String donatorName;
    private BigDecimal amount;
    private String message;
}
