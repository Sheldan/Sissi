package dev.sheldan.sissi.module.rssnews.repository;

import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsFeedSourceRepository extends JpaRepository<NewsFeedSource, Long> {
    Optional<NewsFeedSource> findByName(String name);
    List<NewsFeedSource> findByNameStartsWith(String name);
}
