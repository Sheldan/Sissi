package dev.sheldan.sissi.module.rssnews.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.ChannelUtils;
import dev.sheldan.sissi.module.rssnews.exception.NewsCategoryAlreadyExistsException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.model.template.NewsCategoryChannelMappingInfo;
import dev.sheldan.sissi.module.rssnews.model.template.NewsCategoryInfo;
import dev.sheldan.sissi.module.rssnews.model.template.NewsCategorySubscriptionInfo;
import dev.sheldan.sissi.module.rssnews.service.management.NewsCategoryChannelMappingManagementServiceBean;
import dev.sheldan.sissi.module.rssnews.service.management.NewsCategoryManagementServiceBean;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NewsCategoryServiceBean {

    @Autowired
    private NewsCategoryManagementServiceBean newsCategoryManagementServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private NewsCategoryChannelMappingManagementServiceBean newsCategoryChannelMappingManagementServiceBean;

    public void createCategory(String name, AServer server) {
        if(newsCategoryManagementServiceBean.newsCategoryExistsByNameInServer(name, server)) {
            throw new NewsCategoryAlreadyExistsException();
        }
        newsCategoryManagementServiceBean.createCategory(name, server);
    }

    public void deleteNewsCategoryByName(String name, AServer server) {
        NewsCategory newsCategory = newsCategoryManagementServiceBean.findNewsCategoryByNameInServer(name, server);
        newsCategoryChannelMappingManagementServiceBean.deleteChannelMappingsOfNewsCategory(newsCategory);
        newsCategoryManagementServiceBean.deleteNewsCategory(newsCategory);
    }

    public List<NewsCategory> findNewsCategoriesStartingWith(String name, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        return newsCategoryManagementServiceBean.findNewsCategoriesStartingWith(name, server);
    }

    public List<String> getNamesOfNewsCategoriesStartingWith(String name, Guild guild) {
        return findNewsCategoriesStartingWith(name, guild)
                .stream()
                .map(NewsCategory::getKey)
                .toList();
    }

    public List<NewsCategoryInfo> getCategoryInfos(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        List<NewsCategory> newsCategories = newsCategoryManagementServiceBean.getNewsCategoriesOfServers(server);
        return newsCategories.stream().map(newsCategory -> {
            List<NewsCategorySubscriptionInfo> subscriptionInfos = new ArrayList<>();

            Map<String, List<String>> sourceCategories = new HashMap<>();
            newsCategory
                    .getSourceCategories()
                    .forEach(sourceCategory -> {
                       if(!sourceCategories.containsKey(sourceCategory.getSource().getName())) {
                           sourceCategories.put(sourceCategory.getSource().getName(), new ArrayList<>());
                       }
                       sourceCategories.get(sourceCategory.getSource().getName()).add(sourceCategory.getName());
                    });
            sourceCategories.forEach((sourceName, categoryList) ->
                    subscriptionInfos.add(NewsCategorySubscriptionInfo
                    .builder()
                            .newsFeedName(sourceName)
                            .newsFeedCategories(categoryList)
                    .build()));
            List<NewsCategoryChannelMappingInfo> mappings = new ArrayList<>();
            newsCategory.getMappings().forEach(newsCategoryChannelMapping -> {
                ChannelDisplay channelDisplay = ChannelDisplay
                        .builder()
                        .channelMention(ChannelUtils.getAsMention(newsCategoryChannelMapping.getChannel().getId()))
                        .build();
                mappings.add(NewsCategoryChannelMappingInfo
                .builder()
                        .enabled(newsCategoryChannelMapping.getEnabled())
                        .channel(channelDisplay)
                .build());
            });
            return NewsCategoryInfo
                    .builder()
                    .name(newsCategory.getKey())
                    .subscriptions(subscriptionInfos)
                    .mappings(mappings)
                    .build();
        }).toList();
    }


}
