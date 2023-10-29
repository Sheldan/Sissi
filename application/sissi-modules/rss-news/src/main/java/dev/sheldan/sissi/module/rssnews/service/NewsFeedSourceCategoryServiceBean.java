package dev.sheldan.sissi.module.rssnews.service;

import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSourceCategory;
import dev.sheldan.sissi.module.rssnews.service.management.NewsFeedSourceCategoryManagementServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsFeedSourceCategoryServiceBean {

    @Autowired
    private NewsFeedSourceCategoryManagementServiceBean newsFeedSourceCategoryManagementServiceBean;

    public List<NewsFeedSourceCategory> findNewsFeedSourceCategoriesStartingWith(String name) {
        return newsFeedSourceCategoryManagementServiceBean.findNewsFeedSourceCategoriesNameStartingWith(name);
    }

    public List<NewsFeedSourceCategory> getNewsSourceCategoriesInNewsFeedStartingWith(String name, NewsFeedSource newsFeedSource) {
        return newsFeedSourceCategoryManagementServiceBean.findNewsFeedSourceCategoriesNameStartingWith(name, newsFeedSource);
    }

    public List<String> getNamesOfNewsCategoriesStartingWith(String name) {
        return findNewsFeedSourceCategoriesStartingWith(name)
                .stream()
                .map(NewsFeedSourceCategory::getName)
                .toList();
    }

    public List<String> getNamesOfNewsSourceCategoriesInNewsFeedStartingWith(String name, NewsFeedSource newsFeedSource) {
        return getNewsSourceCategoriesInNewsFeedStartingWith(name, newsFeedSource)
                .stream()
                .map(NewsFeedSourceCategory::getName)
                .toList();
    }


}
