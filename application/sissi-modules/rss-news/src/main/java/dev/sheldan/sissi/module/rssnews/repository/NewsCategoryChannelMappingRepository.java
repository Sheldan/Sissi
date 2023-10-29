package dev.sheldan.sissi.module.rssnews.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategoryChannelMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsCategoryChannelMappingRepository extends JpaRepository<NewsCategoryChannelMapping, Long> {
    boolean existsByChannelAndNewsCategory(AChannel channel, NewsCategory newsCategory);
    void deleteByNewsCategory(NewsCategory newsCategory);
    void deleteByNewsCategoryAndChannel(NewsCategory newsCategory, AChannel channel);
}
