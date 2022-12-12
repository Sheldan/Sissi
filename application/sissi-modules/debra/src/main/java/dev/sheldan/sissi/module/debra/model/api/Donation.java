package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class Donation {
    private BigDecimal amount;
    private String currency;
    private String text;
    private Integer anonym;
    private String firstname;
    private String lastname;
}
