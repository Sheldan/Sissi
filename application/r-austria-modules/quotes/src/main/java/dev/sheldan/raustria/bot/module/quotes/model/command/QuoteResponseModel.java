package dev.sheldan.raustria.bot.module.quotes.model.command;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class QuoteResponseModel {
    private Long quoteId;
    private String authorAvatarURL;
    private String authorName;
    private ServerChannelMessage quotedMessage;
    private String quoteContent;
    private List<String> imageAttachmentURLs;
    private List<String> fileAttachmentURLs;
    private String adderAvatarURL;
    private String adderName;
    private Instant creationDate;
    private String sourceChannelName;
}
