package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsFeedSourceNotFoundException extends AbstractoTemplatableException {

    public NewsFeedSourceNotFoundException() {
        super("News feed source not found.");
    }

    @Override
    public String getTemplateName() {
        return "news_feed_source_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
