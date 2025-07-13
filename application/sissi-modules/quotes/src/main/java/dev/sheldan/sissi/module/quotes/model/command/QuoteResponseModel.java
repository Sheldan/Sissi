package dev.sheldan.sissi.module.quotes.model.command;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class QuoteResponseModel {
    private Long quoteId;
    private UserDisplay authorUserDisplay;
    private MemberDisplay authorMemberDisplay;
    private ServerChannelMessage quotedMessage;
    private String quoteContent;
    private List<String> mediaAttachmentURLs;
    private List<String> fileAttachmentURLs;
    private UserDisplay adderUserDisplay;
    private MemberDisplay adderMemberDisplay;
    private Instant creationDate;
    private String sourceChannelName;
}
