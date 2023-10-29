package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsFeedSourceCategorySubscriptionAlreadyExistsException extends AbstractoTemplatableException {

    public NewsFeedSourceCategorySubscriptionAlreadyExistsException() {
        super("News feed source category subscription already exists.");
    }

    @Override
    public String getTemplateName() {
        return "news_feed_source_category_subscription_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
