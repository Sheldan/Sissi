package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.sissi.module.rssnews.model.database.NewsCategoryChannelMapping;
import dev.sheldan.sissi.module.rssnews.model.database.NewsPost;
import dev.sheldan.sissi.module.rssnews.model.feed.NewsResponseItem;
import dev.sheldan.sissi.module.rssnews.repository.NewsPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsPostManagementServiceBean {

    @Autowired
    private NewsPostRepository newsPostRepository;

    public NewsPost createNewsPost(Long id, NewsResponseItem newsResponseItem, NewsCategoryChannelMapping newsCategoryChannelMapping) {
        NewsPost post = NewsPost
                .builder()
                .url(newsResponseItem.getLink())
                .postChannel(newsCategoryChannelMapping.getChannel())
                .mapping(newsCategoryChannelMapping)
                .server(newsCategoryChannelMapping.getServer())
                .id(id)
                .build();
        return newsPostRepository.save(post);
    }
}
