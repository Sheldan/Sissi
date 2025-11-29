package dev.sheldan.sissi.module.debra.service.management;

import dev.sheldan.sissi.module.debra.model.database.Donation;
import dev.sheldan.sissi.module.debra.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DonationManagementServiceBean {

    @Autowired
    private DonationRepository donationRepository;

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public void updateDonation(Donation donation) {
        donationRepository.save(donation);
    }

    public Donation saveDonation(String hash, Integer count) {
        Donation donation = Donation
                .builder()
                .id(hash)
                .count(count)
                .build();
        return donationRepository.save(donation);
    }

}
