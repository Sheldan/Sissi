package dev.sheldan.sissi.module.quotes.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.sissi.module.quotes.exception.QuoteNotFoundException;
import dev.sheldan.sissi.module.quotes.model.command.QuoteResponseModel;
import dev.sheldan.sissi.module.quotes.model.command.QuoteStatsModel;
import dev.sheldan.sissi.module.quotes.model.database.Quote;
import dev.sheldan.sissi.module.quotes.model.database.QuoteAttachment;
import dev.sheldan.sissi.module.quotes.service.management.QuoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class QuoteServiceBean {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private QuoteManagementService quoteManagementService;

    private static final String QUOTE_RESPONSE_TEMPLATE_KEY = "quote_response";

    public Optional<Quote> getRandomQuoteForMember(AUserInAServer aUserInAServer) {
        // not nice, but good enough for now
        List<Quote> allQuotes = quoteManagementService.getFromAuthor(aUserInAServer);
        if(allQuotes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(allQuotes.get(secureRandom.nextInt(allQuotes.size())));
    }

    public Optional<Quote> getRandomQuote(AServer server) {
        // not nice, but good enough for now
        List<Quote> allQuotes = quoteManagementService.getFromServer(server);
        if(allQuotes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(allQuotes.get(secureRandom.nextInt(allQuotes.size())));
    }

    public void deleteQuote(Long quoteId, AServer server) {
        Optional<Quote> existingQuote = getQuote(quoteId, server);
        if(existingQuote.isPresent()) {
            quoteManagementService.deleteQuote(existingQuote.get());
            log.info("Deleting quote with id {} in server {}.", quoteId, server.getId());
        } else {
            throw new QuoteNotFoundException();
        }
    }

    public Optional<Quote> getQuote(Long quoteId, AServer server) {
        log.info("Loading quote with id {} in server {}.", quoteId, server.getId());
        return quoteManagementService.getQuote(quoteId);
    }

    public CompletableFuture<MessageToSend> renderQuoteToMessageToSend(Quote quote) {
        ServerChannelMessage quotedMessage = ServerChannelMessage
                .builder()
                .messageId(quote.getMessageId())
                .channelId(quote.getSourceChannel().getId())
                .serverId(quote.getServer().getId())
                .build();

        List<String> imageAttachments = quote
                .getAttachments()
                .stream()
                .filter(QuoteAttachment::getIsMedia)
                .map(QuoteAttachment::getUrl)
                .collect(Collectors.toList());

        List<String> fileAttachments = quote
                .getAttachments()
                .stream()
                .filter(quoteAttachment -> !quoteAttachment.getIsMedia())
                .map(QuoteAttachment::getUrl)
                .collect(Collectors.toList());

        QuoteResponseModel.QuoteResponseModelBuilder modelBuilder = QuoteResponseModel
                .builder()
                .quoteContent(quote.getText())
                .mediaAttachmentURLs(imageAttachments)
                .quoteId(quote.getId())
                .fileAttachmentURLs(fileAttachments)
                .creationDate(quote.getCreated())
                .quotedMessage(quotedMessage);
        Long quotedUserId = quote.getAuthor().getUserReference().getId();
        Long quoteAdderUserId = quote.getAdder().getUserReference().getId();
        Long serverId = quote.getServer().getId();
        Long channelId = quote.getSourceChannel().getId();
        Optional<GuildMessageChannel> sourceChannel = channelService.getMessageChannelFromServerOptional(serverId, channelId);
        List<Long> userIds = Arrays.asList(quotedUserId, quoteAdderUserId);
        CompletableFutureList<User> futureList = userService.retrieveUsers(userIds);
        CompletableFuture<MessageToSend> messageFuture = new CompletableFuture<>();
        futureList.getMainFuture().whenComplete((unused, throwable) ->
                createMessageToSend(futureList, modelBuilder, quotedUserId, quoteAdderUserId, serverId, sourceChannel)
                .thenAccept(messageFuture::complete)
                .exceptionally(throwable1 -> {
                    messageFuture.completeExceptionally(throwable1);
                    return null;
                }));
        return messageFuture;

    }

    private CompletableFuture<MessageToSend> createMessageToSend( CompletableFutureList<User> possibleUsers, QuoteResponseModel.QuoteResponseModelBuilder modelBuilder,
                                                                 Long quotedUserId, Long quoteAdderUserId, Long serverId, Optional<GuildMessageChannel> sourceChannel) {
        return memberService.getMembersInServerAsync(serverId, Arrays.asList(quotedUserId, quoteAdderUserId))
                .thenApply(members -> {
                    List<User> foundUsers = possibleUsers.getObjects();
                    Member authorMember = members
                            .stream()
                            .filter(member -> member.getIdLong() == quotedUserId)
                            .findFirst()
                            .orElse(null);
                    Member adderMember = members
                            .stream()
                            .filter(member -> member.getIdLong() == quoteAdderUserId)
                            .findFirst()
                            .orElse(null);
                    User authorUser = foundUsers
                            .stream()
                            .filter(user -> user.getIdLong() == quotedUserId)
                            .findFirst()
                            .orElse(null);
                    User adderUser = foundUsers
                            .stream()
                            .filter(user -> user.getIdLong() == quoteAdderUserId)
                            .findFirst()
                            .orElse(null);
                    String channelName = sourceChannel
                            .map(Channel::getName)
                            .orElse(null);
                    QuoteResponseModel model = modelBuilder
                            .authorMemberDisplay(authorMember != null ? MemberDisplay.fromMember(authorMember) : null)
                            .authorUserDisplay(authorUser != null ? UserDisplay.fromUser(authorUser) : UserDisplay.fromServerUser(ServerUser.fromId(serverId, quotedUserId)))
                            .adderMemberDisplay(adderMember != null ?  MemberDisplay.fromMember(adderMember) : null)
                            .adderUserDisplay(adderUser != null ? UserDisplay.fromUser(adderUser) : UserDisplay.fromServerUser(ServerUser.fromId(serverId, quoteAdderUserId)))
                            .sourceChannelName(channelName)
                            .build();
                    return templateService.renderEmbedTemplate(QUOTE_RESPONSE_TEMPLATE_KEY, model, serverId);
                });
    }

    public Optional<Quote> searchQuote(String query, AServer server) {
        List<Quote> foundQuotes = quoteManagementService.getQuotesWithTextInServer(query, server);
        if(foundQuotes.isEmpty()) {
            return Optional.empty();
        }
        if(foundQuotes.size() > 1) {
            log.info("Found multiple quotes in server {}, returning random one.", server.getId());
            int randomIndex = secureRandom.nextInt(foundQuotes.size());
            return Optional.of(foundQuotes.get(randomIndex));
        } else {
            return Optional.of(foundQuotes.get(0));
        }
    }

    public Optional<Quote> searchQuote(String query, AServer server, Member targetMember) {
        AUserInAServer author = userInServerManagementService.loadOrCreateUser(targetMember);
        List<Quote> foundQuotes = quoteManagementService.getQuotesWithTextInServerFromAuthor(query, server, author);
        if(foundQuotes.isEmpty()) {
            return Optional.empty();
        }
        if(foundQuotes.size() > 1) {
            log.info("Found multiple quotes in server {}, returning random one.", server.getId());
            int randomIndex = secureRandom.nextInt(foundQuotes.size());
            return Optional.of(foundQuotes.get(randomIndex));
        } else {
            return Optional.of(foundQuotes.get(0));
        }
    }

    public QuoteStatsModel getQuoteStats(Member member) {
        AUserInAServer user = userInServerManagementService.loadOrCreateUser(member);
        return getQuoteStats(user, member);
    }
    public QuoteStatsModel getQuoteStats(AUserInAServer user, Member member) {
        Long authored = quoteManagementService.getAmountOfQuotesOfAuthor(user);
        Long added = quoteManagementService.getAmountOfQuotesOfAdder(user);
        return QuoteStatsModel
                .builder()
                .quoteCount(added)
                .authorCount(authored)
                .userName(member.getEffectiveName())
                .userId(user.getUserReference().getId())
                .serverId(user.getServerReference().getId())
                .build();
    }

    public Quote createQuote(ServerUser authorUser, ServerUser adderUser, Message quoteMessage) {
        AUserInAServer author = userInServerManagementService.loadOrCreateUser(authorUser);
        AUserInAServer adder = userInServerManagementService.loadOrCreateUser(adderUser);
        List<Pair<String, Boolean>> attachments = quoteMessage
                .getAttachments()
                .stream()
                .map(attachment -> Pair.of(attachment.getProxyUrl(), attachment.isImage() || attachment.isVideo()))
                .toList();
        return quoteManagementService.createQuote(author, adder, quoteMessage.getContentDisplay(), ServerChannelMessage.fromMessage(quoteMessage), attachments);
    }

    public void deleteByMessageId(Long messageId) {
        Quote quote = quoteManagementService.findByMessage(messageId).orElseThrow(QuoteNotFoundException::new);
        log.info("Deleting quote {} in server {}.", quote.getId(), quote.getServer().getId());
        quoteManagementService.deleteQuote(quote);
    }
}
