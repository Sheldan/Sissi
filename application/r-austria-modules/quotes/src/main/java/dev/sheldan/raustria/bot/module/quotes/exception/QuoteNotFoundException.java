package dev.sheldan.raustria.bot.module.quotes.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class QuoteNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "quote_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
