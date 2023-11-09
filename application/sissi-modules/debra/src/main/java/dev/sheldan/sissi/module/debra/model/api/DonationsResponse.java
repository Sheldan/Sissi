package dev.sheldan.sissi.module.debra.model.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Builder
public class DonationsResponse {
    @SerializedName("page")
    private Description page;
    @SerializedName("donation_count")
    private BigInteger donationCount;
    @SerializedName("donations")
    private List<Donation> donations;
}
