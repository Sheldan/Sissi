package dev.sheldan.sissi.module.rssnews.service.management;

import dev.sheldan.sissi.module.rssnews.model.database.NewsFeedRecord;
import dev.sheldan.sissi.module.rssnews.model.feed.NewsResponseItem;
import dev.sheldan.sissi.module.rssnews.repository.NewsFeedRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsFeedRecordManagementServiceBean {

    @Autowired
    private NewsFeedRecordRepository repository;

    public List<NewsFeedRecord> getAllRecords() {
        return repository.findAll();
    }

    public NewsFeedRecord createRecord(NewsResponseItem newsResponseItem) {
        NewsFeedRecord newsFeedRecord = NewsFeedRecord
                .builder()
                .source(newsResponseItem.getNewsFeedSource())
                .url(newsResponseItem.getLink())
                .build();
        return repository.save(newsFeedRecord);
    }
}
