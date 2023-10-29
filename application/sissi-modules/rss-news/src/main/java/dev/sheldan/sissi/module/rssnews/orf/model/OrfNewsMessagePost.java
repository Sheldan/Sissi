package dev.sheldan.sissi.module.rssnews.orf.model;

import dev.sheldan.sissi.module.rssnews.model.feed.NewsResponseItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrfNewsMessagePost {
    private String title;
    private String url;
    private String category;
    private String description;
    private String imageURL;

    public static OrfNewsMessagePost fromNewsResponseItem(NewsResponseItem newsResponseItem) {
        return OrfNewsMessagePost
                .builder()
                .category(newsResponseItem.getMainCategory())
                .description(newsResponseItem.getDescription())
                .imageURL(newsResponseItem.getImageURL())
                .title(newsResponseItem.getTitle())
                .url(newsResponseItem.getLink())
                .build();
    }
}
