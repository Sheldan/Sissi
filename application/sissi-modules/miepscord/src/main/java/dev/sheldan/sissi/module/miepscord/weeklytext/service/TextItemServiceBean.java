package dev.sheldan.sissi.module.miepscord.weeklytext.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.sissi.module.miepscord.weeklytext.config.WeeklyTextPostTarget;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.database.TextItem;
import dev.sheldan.sissi.module.miepscord.weeklytext.model.template.TextItemPostModel;
import dev.sheldan.sissi.module.miepscord.weeklytext.service.management.TextItemServiceManagementBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.sissi.module.miepscord.weeklytext.config.WeeklyTextFeatureConfig.WEEKLY_TEXT_SERVER_ID;

@Component
@Slf4j
public class TextItemServiceBean {

    public static final String WEEKLY_TEXT_ITEM_POST_TEMPLATE = "weekly_text_item_post";
    @Autowired
    private TextItemServiceManagementBean textItemServiceManagementBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TextItemServiceBean self;

    public TextItem createTextItem(String text, Member creator) {
        log.info("Creating new text item in server {} from user {}.", creator.getGuild().getIdLong(), creator.getIdLong());
        AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
        return textItemServiceManagementBean.createTextItem(text, creatorUser);
    }

    public CompletableFuture<Void> sendTexItem() {
        Long serverId = Long.parseLong(System.getenv(WEEKLY_TEXT_SERVER_ID));
        Optional<TextItem> nextTextItemOptional = textItemServiceManagementBean.getNextTextItem();
        if(nextTextItemOptional.isEmpty()) {
            log.info("No next text item found.");
            return CompletableFuture.completedFuture(null);
        }
        TextItem textItem = nextTextItemOptional.get();
        log.info("Sending text item {}.", textItem.getId());
        Long textItemId = textItem.getId();
        TextItemPostModel model = TextItemPostModel
                .builder()
                .id(textItemId)
                .created(textItem.getCreated())
                .text(textItem.getText())
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(WEEKLY_TEXT_ITEM_POST_TEMPLATE, model, serverId);
        List<CompletableFuture<Message>> futures = postTargetService.sendEmbedInPostTarget(messageToSend, WeeklyTextPostTarget.TEXT_ITEM_TARGET, serverId);
        CompletableFutureList<Message> futureList = new CompletableFutureList<>(futures);
        return futureList.getMainFuture().thenAccept(unused -> {
            self.setTexItemToDone(textItemId);
        });
    }

    @Transactional
    public void setTexItemToDone(Long id) {
        Optional<TextItem> textItemOptional = textItemServiceManagementBean.findTextItemById(id);
        textItemOptional.ifPresentOrElse(textItem -> {
            log.info("Setting textItem with ID {} to done.", id);
            textItem.setDone(true);
        }, () -> log.info("TextItem {} not found.", id));
    }

    public void removeTextItem(Long id) {
        log.info("Deleting text item with ID {}.", id);
        textItemServiceManagementBean.deleteById(id);
    }
}
