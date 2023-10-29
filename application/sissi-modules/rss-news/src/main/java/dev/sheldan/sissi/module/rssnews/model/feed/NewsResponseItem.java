package dev.sheldan.sissi.module.rssnews.model.feed;

import com.apptasticsoftware.rssreader.Item;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSource;
import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedSourceCategoryType;
import dev.sheldan.sissi.module.rssnews.model.feed.rfd.RFDItem;
import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseItem {
    private String title;
    private String link;
    private String mainCategory;
    private String date;
    private String secondCategory;
    private String description;
    private String imageURL;
    private NewsFeedSourceCategoryType type;
    private NewsFeedSource newsFeedSource;

    public static NewsResponseItem fromRFDItem(RFDItem item) {
        return NewsResponseItem
                .builder()
                .title(item.getTitle())
                .date(item.getDate())
                .description(item.getDescription())
                .mainCategory(item.getSubject())
                .secondCategory(item.getOewaCategory())
                .type(NewsFeedSourceCategoryType.RFD)
                .link(item.getLink())
                .build();
    }

    public static NewsResponseItem fromRSSItem(Item item) {
        String imageUrl = null;
        if(item.getEnclosure().isPresent()) {
            imageUrl = item.getEnclosure().get().getUrl();
        }
        return NewsResponseItem
                .builder()
                .title(item.getTitle().orElse(null))
                .date(item.getPubDate().orElse(null))
                .description(item.getDescription().orElse(null))
                .mainCategory(item.getCategory().orElse(null))
                .type(NewsFeedSourceCategoryType.RSS)
                .link(item.getLink().orElse(null))
                .imageURL(imageUrl)
                .build();
    }
}
