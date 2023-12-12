package dev.sheldan.sissi.module.debra.model.api;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class EndlessStreamInfo {
    private Instant endDate;
    private Instant startDate;
    private Long donationAmount;
    private Long minuteRate;
}
