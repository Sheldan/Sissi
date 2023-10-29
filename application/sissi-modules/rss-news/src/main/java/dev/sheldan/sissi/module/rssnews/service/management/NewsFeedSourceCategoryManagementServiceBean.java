package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.sissi.module.rssnews.exception.NewsFeedSourceCategoryNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSourceCategory;
import dev.sheldan.sissi.module.rssnews.repository.NewsFeedSourceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NewsFeedSourceCategoryManagementServiceBean {

    @Autowired
    private NewsFeedSourceCategoryRepository repository;

    public List<NewsFeedSourceCategory> getAllCategories(){
        return repository.findAll();
    }

    public NewsFeedSourceCategory createCategory(String name, NewsFeedSource newsFeedSource) {
        NewsFeedSourceCategory category = NewsFeedSourceCategory
                .builder()
                .source(newsFeedSource)
                .name(name)
                .build();
        return repository.save(category);
    }

    public List<NewsFeedSourceCategory> findNewsFeedSourceCategoriesNameStartingWith(String name) {
        return repository.findByNameStartsWith(name);
    }

    public List<NewsFeedSourceCategory> findNewsFeedSourceCategoriesNameStartingWith(String name, NewsFeedSource newsFeedSource) {
        return repository.findByNameStartsWithAndSource(name, newsFeedSource);
    }

    public Optional<NewsFeedSourceCategory> findNewsFeedSourceCategoryByNameAndNewsFeedSourceOptional(String name, NewsFeedSource newsFeedSource) {
        return repository.findByNameAndSource(name, newsFeedSource);
    }

    public NewsFeedSourceCategory findNewsFeedSourceCategoryByNameAndNewsFeedSource(String name, NewsFeedSource newsFeedSource) {
        return findNewsFeedSourceCategoryByNameAndNewsFeedSourceOptional(name, newsFeedSource).orElseThrow(NewsFeedSourceCategoryNotFoundException::new);
    }
}
