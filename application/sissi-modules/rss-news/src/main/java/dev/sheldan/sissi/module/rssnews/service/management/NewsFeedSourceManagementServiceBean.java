package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.sissi.module.rssnews.exception.NewsFeedSourceNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.repository.NewsFeedSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NewsFeedSourceManagementServiceBean {

    @Autowired
    private NewsFeedSourceRepository newsFeedSourceRepository;

    public List<NewsFeedSource> getAllSources() {
        return newsFeedSourceRepository.findAll();
    }

    public Optional<NewsFeedSource> getNewsFeedSourceWithNameOptional(String name) {
        return newsFeedSourceRepository.findByName(name);
    }

    public NewsFeedSource getNewsFeedSourceWithName(String name) {
        return getNewsFeedSourceWithNameOptional(name).orElseThrow(NewsFeedSourceNotFoundException::new);
    }

    public List<NewsFeedSource> getNewsFeedSourcesStartingWithName(String name) {
        return newsFeedSourceRepository.findByNameStartsWith(name);
    }

    public List<String> getNewsFeedSourceNamesStartingWithName(String name) {
        return getNewsFeedSourcesStartingWithName(name)
                .stream()
                .map(NewsFeedSource::getName)
                .toList();
    }

}
