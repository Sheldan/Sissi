package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.sissi.module.rssnews.exception.NewsCategoryChannelMappingNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategoryChannelMapping;
import dev.sheldan.sissi.module.rssnews.repository.NewsCategoryChannelMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsCategoryChannelMappingManagementServiceBean {

    @Autowired
    private NewsCategoryChannelMappingRepository repository;

    public NewsCategoryChannelMapping createNewsCategoryChannelMapping(NewsCategory newsCategory, AChannel channel) {
        NewsCategoryChannelMapping category = NewsCategoryChannelMapping
                .builder()
                .channel(channel)
                .server(channel.getServer())
                .newsCategory(newsCategory)
                .enabled(true)
                .build();
        return repository.save(category);
    }

    public NewsCategoryChannelMapping getChannelMappingById(Long id) {
        return repository.findById(id).orElseThrow(NewsCategoryChannelMappingNotFoundException::new);
    }

    public boolean newsCategoryChannelMappingExists(NewsCategory newsCategory, AChannel channel) {
        return repository.existsByChannelAndNewsCategory(channel, newsCategory);
    }

    public void deleteChannelMappingsOfNewsCategory(NewsCategory newsCategory) {
        repository.deleteByNewsCategory(newsCategory);
    }

    public void deleteNewsCategoryChannelMapping(NewsCategory newsCategory, AChannel channel) {
        repository.deleteByNewsCategoryAndChannel(newsCategory, channel);
    }

    public void deleteNewsCategoryChannelMapping(NewsCategoryChannelMapping mapping) {
        repository.delete(mapping);
    }

}
