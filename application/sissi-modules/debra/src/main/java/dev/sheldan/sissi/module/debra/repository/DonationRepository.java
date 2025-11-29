package dev.sheldan.sissi.module.debra.repository;

import dev.sheldan.sissi.module.debra.model.database.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, String> {
}
