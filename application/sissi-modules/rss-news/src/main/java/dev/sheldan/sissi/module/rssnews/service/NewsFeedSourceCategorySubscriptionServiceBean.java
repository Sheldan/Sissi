package dev.sheldan.sissi.module.rssnews.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.sissi.module.rssnews.exception.NewsFeedSourceCategorySubscriptionAlreadyExistsException;
import dev.sheldan.sissi.module.rssnews.exception.NewsFeedSourceCategorySubscriptionNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSourceCategory;
import dev.sheldan.sissi.module.rssnews.service.management.NewsCategoryManagementServiceBean;
import dev.sheldan.sissi.module.rssnews.service.management.NewsFeedSourceCategoryManagementServiceBean;
import dev.sheldan.sissi.module.rssnews.service.management.NewsFeedSourceManagementServiceBean;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsFeedSourceCategorySubscriptionServiceBean {

    @Autowired
    private NewsCategoryManagementServiceBean newsCategoryManagementServiceBean;

    @Autowired
    private NewsFeedSourceCategoryManagementServiceBean newsFeedSourceCategoryManagementServiceBean;

    @Autowired
    private NewsFeedSourceManagementServiceBean newsFeedSourceManagementServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    public void createNewsFeedSourceCategorySubscription(String categoryName, String sourceCategoryName, String newsFeedSourceName, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        NewsCategory newsCategory = newsCategoryManagementServiceBean.findNewsCategoryByNameInServer(categoryName, server);
        NewsFeedSource newsFeedSource = newsFeedSourceManagementServiceBean.getNewsFeedSourceWithName(newsFeedSourceName);
        NewsFeedSourceCategory sourceCategory = newsFeedSourceCategoryManagementServiceBean.findNewsFeedSourceCategoryByNameAndNewsFeedSource(sourceCategoryName, newsFeedSource);
        if(newsCategory.getSourceCategories().contains(sourceCategory)) {
            throw new NewsFeedSourceCategorySubscriptionAlreadyExistsException();
        }
        newsCategory.getSourceCategories().add(sourceCategory);
        sourceCategory.getCategories().add(newsCategory);
    }

    public void deleteNewsFeedSourceCategorySubscription(String categoryName, String sourceCategoryName, String newsFeedSourceName, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        NewsCategory newsCategory = newsCategoryManagementServiceBean.findNewsCategoryByNameInServer(categoryName, server);
        NewsFeedSource newsFeedSource = newsFeedSourceManagementServiceBean.getNewsFeedSourceWithName(newsFeedSourceName);
        NewsFeedSourceCategory sourceCategory = newsFeedSourceCategoryManagementServiceBean.findNewsFeedSourceCategoryByNameAndNewsFeedSource(sourceCategoryName, newsFeedSource);
        if(!newsCategory.getSourceCategories().contains(sourceCategory)) {
            throw new NewsFeedSourceCategorySubscriptionNotFoundException();
        }
        newsCategory.getSourceCategories().remove(sourceCategory);
        sourceCategory.getCategories().remove(newsCategory);
    }
}
