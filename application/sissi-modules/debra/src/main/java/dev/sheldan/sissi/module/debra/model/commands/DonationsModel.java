package dev.sheldan.sissi.module.debra.model.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Setter
public class DonationsModel {
    private BigDecimal totalAmount;
    private DonationType type;
    private List<DonationItemModel> donations;

    public enum DonationType {
        LATEST, TOP
    }

}
