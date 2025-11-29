package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@ToString
public class DonationDto {
    private BigDecimal amount;
    private String currency;
    private String text;
    private Boolean anonymous;
    private String name;
    private LocalDate date;

    public String stringRepresentation() {
        return String.format("%s %s %s %s %s", name, amount, text, anonymous, date.format(DateTimeFormatter.ISO_DATE));
    }
}
