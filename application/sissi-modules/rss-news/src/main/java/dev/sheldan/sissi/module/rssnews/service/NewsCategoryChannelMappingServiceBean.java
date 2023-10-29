package dev.sheldan.sissi.module.rssnews.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.sissi.module.rssnews.exception.NewsCategoryChannelMappingAlreadyExistsException;
import dev.sheldan.sissi.module.rssnews.exception.NewsCategoryChannelMappingNotFoundException;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategory;
import dev.sheldan.sissi.module.rssnews.model.database.NewsCategoryChannelMapping;
import dev.sheldan.sissi.module.rssnews.service.management.NewsCategoryChannelMappingManagementServiceBean;
import dev.sheldan.sissi.module.rssnews.service.management.NewsCategoryManagementServiceBean;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsCategoryChannelMappingServiceBean {

    @Autowired
    private NewsCategoryManagementServiceBean newsCategoryManagementServiceBean;

    @Autowired
    private NewsCategoryChannelMappingManagementServiceBean newsCategoryChannelMappingManagementServiceBean;

    @Autowired
    private ChannelManagementService channelManagementService;

    public NewsCategoryChannelMapping createNewsCategoryChannelMapping(String categoryName, GuildChannel channel) {
        AChannel aChannel = channelManagementService.loadChannel(channel.getIdLong());
        NewsCategory newsCategory = newsCategoryManagementServiceBean.findNewsCategoryByNameInServer(categoryName, aChannel.getServer());
        if(newsCategoryChannelMappingManagementServiceBean.newsCategoryChannelMappingExists(newsCategory, aChannel)) {
            throw new NewsCategoryChannelMappingAlreadyExistsException();
        }
        return newsCategoryChannelMappingManagementServiceBean.createNewsCategoryChannelMapping(newsCategory, aChannel);
    }

    public void deleteNewsCategoryChannelMapping(String categoryName, GuildChannel channel) {
        AChannel aChannel = channelManagementService.loadChannel(channel.getIdLong());
        NewsCategory newsCategory = newsCategoryManagementServiceBean.findNewsCategoryByNameInServer(categoryName, aChannel.getServer());
        if(!newsCategoryChannelMappingManagementServiceBean.newsCategoryChannelMappingExists(newsCategory, aChannel)) {
            throw new NewsCategoryChannelMappingNotFoundException();
        }
        newsCategoryChannelMappingManagementServiceBean.deleteNewsCategoryChannelMapping(newsCategory, aChannel);
    }
}
