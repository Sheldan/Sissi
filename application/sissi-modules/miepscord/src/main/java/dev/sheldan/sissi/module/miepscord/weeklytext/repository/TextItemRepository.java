package dev.sheldan.sissi.module.miepscord.weeklytext.repository;

import dev.sheldan.sissi.module.miepscord.weeklytext.model.database.TextItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextItemRepository extends JpaRepository<TextItem, Long> {
    List<TextItem> findByDoneFalseOrderByCreated();
    List<TextItem> findByDone(Boolean done);
}
