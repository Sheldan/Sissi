package dev.sheldan.sissi.module.rssnews.orf.service;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.sissi.module.rssnews.model.database.*;
import dev.sheldan.sissi.module.rssnews.orf.config.OrfNewsFeatureConfig;
import dev.sheldan.sissi.module.rssnews.model.feed.NewsResponseItem;
import dev.sheldan.sissi.module.rssnews.model.feed.rfd.RFDItem;
import dev.sheldan.sissi.module.rssnews.orf.model.OrfNewsMessagePost;
import dev.sheldan.sissi.module.rssnews.service.management.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class OrfNewsService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private NewsFeedSourceManagementServiceBean newsFeedSourceManagementBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private OrfNewsFeatureConfig orfNewsFeatureConfig;

    @Autowired
    private NewsFeedRecordManagementServiceBean newsFeedRecordManagementBean;

    @Autowired
    private NewsCategoryManagementServiceBean newsCategoryManagementBean;

    @Autowired
    private NewsFeedSourceCategoryManagementServiceBean newsFeedSourceCategoryManagementBean;

    @Autowired
    private NewsFeedRecordManagementServiceBean newsFeedRecordManagementServiceBean;

    @Autowired
    private NewsCategoryChannelMappingManagementServiceBean newsCategoryChannelMappingManagementServiceBean;

    @Autowired
    private NewsPostManagementServiceBean newsPostManagementServiceBean;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private OrfNewsService self;

    private static final String ORF_NEWS_POST_TEMPLATE_KEY = "orf_news_post";

    @Transactional
    public void checkNewsPosts() {
        List<AServer> servers = serverManagementService.getAllServers();
        List<AServer> serversWithEnabledFeature = servers
                .stream()
                .filter(server -> featureFlagService.isFeatureEnabled(orfNewsFeatureConfig, server))
                .toList();
        if(serversWithEnabledFeature.isEmpty()) {
            log.info("No servers have orf news enabled - skipping execution.");
            return;
        }
        List<NewsFeedSource> sources = newsFeedSourceManagementBean.getAllSources();
        List<NewsResponseItem> newsItems = new ArrayList<>();
        sources.forEach(source -> newsItems.addAll(loadItemsFromNewsSource(source)));
        Set<String> existingRecordUrls = newsFeedRecordManagementBean.getAllRecords()
                .stream()
                .map(NewsFeedRecord::getUrl)
                .collect(Collectors.toSet());
        List<NewsResponseItem> newItems = newsItems
                .stream()
                .filter(newsResponseItem -> !existingRecordUrls.contains(newsResponseItem.getLink()))
                .toList();

        List<NewsCategory> categoriesToCover = newsCategoryManagementBean.getNewsCategoriesOfServers(serversWithEnabledFeature);
        categoriesToCover.forEach(category -> sendNewsItemsToNewsCategory(newItems, category));
        createNewCategories(newsItems);
        saveNewsRecords(newItems);
    }

    private void saveNewsRecords(List<NewsResponseItem> newItems) {
        newItems.forEach(newsResponseItem -> newsFeedRecordManagementServiceBean.createRecord(newsResponseItem));
    }

    private void createNewCategories(List<NewsResponseItem> newsItems) {
        Set<String> allExistingCategories = newsFeedSourceCategoryManagementBean.getAllCategories()
                .stream()
                .map(NewsFeedSourceCategory::getName)
                .collect(Collectors.toSet());
        Set<String> incomingCategories = newsItems
                .stream()
                .map(NewsResponseItem::getMainCategory)
                .collect(Collectors.toSet());

        HashSet<Object> seenCategories = new HashSet<>();
        newsItems.removeIf(e -> !seenCategories.add(e.getMainCategory()));

        Map<String, NewsResponseItem> categoryToNewsItem = newsItems
                .stream()
                .collect(Collectors.toMap(NewsResponseItem::getMainCategory, Function.identity()));
        incomingCategories.removeAll(allExistingCategories);
        incomingCategories.removeIf(Objects::isNull);
        if(!incomingCategories.isEmpty()) {
            incomingCategories.forEach(categoryName ->
                    newsFeedSourceCategoryManagementBean.createCategory(categoryName, categoryToNewsItem.get(categoryName).getNewsFeedSource()));
        }
    }

    private void sendNewsItemsToNewsCategory(List<NewsResponseItem> newsItems, NewsCategory category) {
        if(!category.getEnabled()) {
            return;
        }
        Set<String> categoriesToSend = category
                .getSourceCategories()
                .stream()
                .map(NewsFeedSourceCategory::getName)
                .collect(Collectors.toSet());
        List<NewsResponseItem> itemsToSend = newsItems
                .stream()
                .filter(newsResponseItem -> categoriesToSend.contains(newsResponseItem.getMainCategory()))
                .toList();
        category.getMappings().forEach(newsCategoryChannelMapping -> sendNewsItemToChannel(itemsToSend, newsCategoryChannelMapping));
    }

    private void sendNewsItemToChannel(List<NewsResponseItem> items, NewsCategoryChannelMapping mapping) {
        items.forEach(item -> {
            Long channelId = mapping.getChannel().getId();
            Long serverId = mapping.getServer().getId();
            Long mappingId = mapping.getId();
            OrfNewsMessagePost orfNewsMessagePostModel = OrfNewsMessagePost.fromNewsResponseItem(item);
            MessageToSend messageToSend = templateService.renderEmbedTemplate(ORF_NEWS_POST_TEMPLATE_KEY, orfNewsMessagePostModel, serverId);
            CompletableFutureList<Message> futureList = new CompletableFutureList<>(channelService.sendMessageEmbedToSendToAChannel(messageToSend, mapping.getChannel()));
            futureList.getMainFuture().thenAccept(unused -> {
                log.info("Sent news post {} to channel {}.", item.getTitle(), channelId);
                Long messageId = futureList.getObjects().get(0).getIdLong();
                self.persistNewsPost(messageId, mappingId, item);
            }).exceptionally(throwable -> {
                log.warn("Failed to send news post {} to channel {}.", item.getTitle(), channelId);
                return null;
            });
        });
    }

    @Transactional
    public void persistNewsPost(Long messageId, Long mappingId, NewsResponseItem item) {
        NewsCategoryChannelMapping channelMapping = newsCategoryChannelMappingManagementServiceBean.getChannelMappingById(mappingId);
        newsPostManagementServiceBean.createNewsPost(messageId, item, channelMapping);
    }

    public List<NewsResponseItem> loadItemsFromNewsSource(NewsFeedSource source) {
        Request request = new Request.Builder()
                .url(source.getUrl())
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            return switch (source.getType()) {
                case RFD -> loadNewsSourceRFD(response, source);
                case RSS -> loadNewsSourceRSS(response, source);
            };
        } catch (IOException ex) {
            throw new AbstractoRunTimeException(String.format("Failed to gather news from %s", source.getName()), ex);
        }
    }

    private List<NewsResponseItem> loadNewsSourceRSS(Response response, NewsFeedSource source) {
        RssReader reader = new RssReader();
        try (response) {
            InputStream is = response.body().byteStream();
            Stream<Item> rssFeed = reader.read(is);
            return rssFeed.map(NewsResponseItem::fromRSSItem)
                    .peek(newsResponseItem -> newsResponseItem.setNewsFeedSource(source))
                    .toList();
        }
    }

    private List<NewsResponseItem> loadNewsSourceRFD(Response response, NewsFeedSource source) throws IOException {
        Map<String, RFDItem> items = new HashMap<>();
        final Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(response.body().bytes()), null);
        StmtIterator statements = model.listStatements();
        while(statements.hasNext()) {
            Statement x = statements.nextStatement();
            String article = x.getSubject().toString();
            RFDItem item;
            if(!items.containsKey(article)) {
                item = new RFDItem();
            } else {
                item = items.get(article);
            }
            String predicate = x.getPredicate().toString();
            String objectValue = x.getObject().toString();
            switch (predicate) {
                case "http://rss.orf.at/1.0/oewaCategory":
                    item.setOewaCategory(objectValue);
                    break;
                case "http://purl.org/rss/1.0/link":
                    item.setLink(objectValue);
                    break;
                case "http://purl.org/dc/elements/1.1/subject":
                    item.setSubject(objectValue);
                    break;
                case "http://purl.org/dc/elements/1.1/date":
                    item.setDate(objectValue);
                    break;
                case "http://purl.org/rss/1.0/description":
                    item.setDescription(objectValue);
                    break;
                case "http://purl.org/rss/1.0/title":
                    item.setTitle(objectValue);
                    break;
            }
            items.putIfAbsent(article, item);
        }
        Set<String> uselessEntries = new HashSet<>();
        items.forEach((s, rfdItem) -> {
            if(rfdItem.getLink() == null) {
                uselessEntries.add(s);
            }
        });
        uselessEntries.forEach(items::remove);
        return items
                .values()
                .stream()
                .map(NewsResponseItem::fromRFDItem)
                .peek(newsResponseItem -> newsResponseItem.setNewsFeedSource(source))
                .toList();
    }

}
