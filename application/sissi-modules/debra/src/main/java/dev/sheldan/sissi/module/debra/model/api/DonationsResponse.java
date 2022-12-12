package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@Builder
public class DonationsResponse {
    private Description page;
    private BigInteger donationCount;
    private List<Donation> donations;
}
