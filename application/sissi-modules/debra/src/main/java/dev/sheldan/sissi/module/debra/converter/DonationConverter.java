package dev.sheldan.sissi.module.debra.converter;

import dev.sheldan.sissi.module.debra.model.api.DonationDto;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
public class DonationConverter {
    public DonationItemModel convertDonation(DonationDto donation) {
        return DonationItemModel
                .builder()
                .donationAmount(donation.getAmount())
                .name(donation.getName())
                .anonymous(BooleanUtils.toBoolean(donation.getAnonymous()))
                .build();
    }

    public DonationsModel convertDonationResponse(DonationsResponse response) {
        return DonationsModel
                .builder()
                .totalAmount(response.getCurrentDonationAmount())
                .build();
    }
}
