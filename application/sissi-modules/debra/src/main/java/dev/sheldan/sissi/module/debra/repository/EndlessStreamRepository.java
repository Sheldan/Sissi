package dev.sheldan.sissi.module.debra.repository;

import dev.sheldan.sissi.module.debra.model.database.EndlessStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndlessStreamRepository extends JpaRepository<EndlessStream, Long> {
}
