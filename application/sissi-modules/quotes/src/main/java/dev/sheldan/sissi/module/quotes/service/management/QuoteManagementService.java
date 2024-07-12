package dev.sheldan.sissi.module.quotes.service.management;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.sissi.module.quotes.model.database.Quote;
import dev.sheldan.sissi.module.quotes.model.database.QuoteAttachment;
import dev.sheldan.sissi.module.quotes.repository.QuoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class QuoteManagementService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    public Quote createQuote(AUserInAServer author, AUserInAServer adder, String messageText, ServerChannelMessage quotedMessage, List<Pair<String, Boolean>> attachments) {
        AChannel channel = channelManagementService.loadChannel(quotedMessage.getChannelId());

        Quote quote = Quote
                .builder()
                .adder(adder)
                .author(author)
                .text(messageText)
                .messageId(quotedMessage.getMessageId())
                .server(adder.getServerReference())
                .sourceChannel(channel)
                .build();
        List<QuoteAttachment> quoteAttachments = attachments
                .stream()
                .map(stringBooleanPair -> QuoteAttachment
                        .builder()
                        .url(stringBooleanPair.getLeft())
                        .quote(quote)
                        .server(adder.getServerReference())
                        .isImage(stringBooleanPair.getRight())
                        .build())
                .toList();

        log.info("Creating quote from {} added by {} in server {}.", author.getUserReference().getId(), adder.getUserReference().getId(), author.getServerReference().getId());

        quote.setAttachments(quoteAttachments);

        return quoteRepository.save(quote);
    }

    public List<Quote> getFromAuthor(AUserInAServer author) {
        return quoteRepository.findByAuthor(author);
    }

    public List<Quote> getFromServer(AServer aServer) {
        return quoteRepository.findByServer(aServer);
    }

    public void deleteQuote(Quote quote) {
        quoteRepository.delete(quote);
    }

    public Optional<Quote> getQuote(Long quoteId) {
        return quoteRepository.findById(quoteId);
    }

    public List<Quote> getQuotesWithTextInServer(String text, AServer server) {
        return quoteRepository.findByTextContainingAndServer(text, server);
    }

    public List<Quote> getQuotesWithTextInServerFromAuthor(String text, AServer server, AUserInAServer aUserInAServer) {
        return quoteRepository.findByTextContainingAndServerAndAuthor(text, server, aUserInAServer);
    }

    public Long getAmountOfQuotesOfAuthor(AUserInAServer aUserInAServer) {
        return quoteRepository.countByAuthor(aUserInAServer);
    }

    public Long getAmountOfQuotesOfAdder(AUserInAServer aUserInAServer) {
        return quoteRepository.countByAdder(aUserInAServer);
    }

    public Optional<Quote> findByMessage(Long messageId) {
        return quoteRepository.findByMessageId(messageId);
    }
}
