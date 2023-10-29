package dev.sheldan.sissi.module.rssnews.repository;

import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsFeedSourceCategoryRepository extends JpaRepository<NewsFeedSourceCategory, Long> {
    List<NewsFeedSourceCategory> findByNameStartsWith(String name);
    List<NewsFeedSourceCategory> findByNameStartsWithAndSource(String name, NewsFeedSource newsFeedSource);
    Optional<NewsFeedSourceCategory> findByNameAndSource(String name, NewsFeedSource newsFeedSource);
}
