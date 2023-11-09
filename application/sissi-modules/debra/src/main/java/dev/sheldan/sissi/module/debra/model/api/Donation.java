package dev.sheldan.sissi.module.debra.model.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class Donation {
    @SerializedName("amount")
    private BigDecimal amount;
    @SerializedName("currency")
    private String currency;
    @SerializedName("text")
    private String text;
    @SerializedName("anonym")
    private Integer anonym;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("lastname")
    private String lastname;
}
