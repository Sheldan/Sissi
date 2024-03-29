package dev.sheldan.sissi.module.debra.model.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class Description {
    @SerializedName("collected")
    private BigDecimal collected;
    @SerializedName("target")
    private BigDecimal target;
    @SerializedName("currency")
    private String currency;
    @SerializedName("slug")
    private String slug;
    @SerializedName("displayname")
    private String displayName;
    @SerializedName("collectednet")
    private BigDecimal collectedNet;
    @SerializedName("percent")
    private BigDecimal percent;
}
