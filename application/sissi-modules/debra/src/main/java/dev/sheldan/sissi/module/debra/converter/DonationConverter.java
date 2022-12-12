package dev.sheldan.sissi.module.debra.converter;

import dev.sheldan.sissi.module.debra.model.api.Donation;
import dev.sheldan.sissi.module.debra.model.api.DonationsResponse;
import dev.sheldan.sissi.module.debra.model.commands.DonationItemModel;
import dev.sheldan.sissi.module.debra.model.commands.DonationsModel;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
public class DonationConverter {
    public DonationItemModel convertDonation(Donation donation) {
        return DonationItemModel
                .builder()
                .donationAmount(donation.getAmount())
                .firstName(donation.getFirstname())
                .lastName(donation.getLastname())
                .anonymous(BooleanUtils.toBoolean(donation.getAnonym()))
                .build();
    }

    public DonationsModel convertDonationResponse(DonationsResponse response) {
        return DonationsModel
                .builder()
                .totalAmount(response.getPage().getCollected())
                .build();
    }
}
