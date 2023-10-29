package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.sissi.module.rssnews.exception.NewsCategoryNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.repository.NewsCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NewsCategoryManagementServiceBean {

    @Autowired
    private NewsCategoryRepository repository;

    public List<NewsCategory> getNewsCategoriesOfServers(List<AServer> servers) {
        return repository.getByServerIn(servers);
    }

    public List<NewsCategory> getNewsCategoriesOfServers(AServer server) {
        return repository.getByServer(server);
    }

    public NewsCategory createCategory(String name, AServer server) {
        NewsCategory category = NewsCategory
                .builder()
                .key(name)
                .server(server)
                .enabled(true)
                .build();
        return repository.save(category);
    }

    public Optional<NewsCategory> findNewsCategoryByNameInServerOptional(String name, AServer server) {
        return repository.findByKeyAndServer(name, server);
    }

    public NewsCategory findNewsCategoryByNameInServer(String name, AServer server) {
        return findNewsCategoryByNameInServerOptional(name, server).orElseThrow(NewsCategoryNotFoundException::new);
    }

    public boolean newsCategoryExistsByNameInServer(String name, AServer server) {
        return repository.existsByKeyAndServer(name, server);
    }

    public void deleteNewsCategory(NewsCategory newsCategory) {
        repository.delete(newsCategory);
    }

    public List<NewsCategory> findNewsCategoriesStartingWith(String name, AServer server) {
        return repository.findByKeyStartsWithAndServer(name, server);
    }

}
