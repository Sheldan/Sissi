package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsFeedSourceCategoryNotFoundException extends AbstractoTemplatableException {

    public NewsFeedSourceCategoryNotFoundException() {
        super("News feed source category not found.");
    }

    @Override
    public String getTemplateName() {
        return "news_feed_source_category_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
