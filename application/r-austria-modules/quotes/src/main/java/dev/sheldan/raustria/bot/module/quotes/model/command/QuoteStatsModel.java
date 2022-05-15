package dev.sheldan.raustria.bot.module.quotes.model.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuoteStatsModel {
    private Long authorCount;
    private String userName;
    private Long quoteCount;
    private Long userId;
    private Long serverId;
}
