package dev.sheldan.sissi.module.rssnews.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    List<NewsCategory> getByServerIn(List<AServer> servers);
    List<NewsCategory> getByServer(AServer server);
    Optional<NewsCategory> findByKeyAndServer(String name, AServer server);
    boolean existsByKeyAndServer(String name, AServer server);
    List<NewsCategory> findByKeyStartsWithAndServer(String name, AServer server);
}
