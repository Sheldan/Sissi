package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsFeedSourceCategorySubscriptionNotFoundException extends AbstractoTemplatableException {

    public NewsFeedSourceCategorySubscriptionNotFoundException() {
        super("News feed source category subscription not found.");
    }

    @Override
    public String getTemplateName() {
        return "news_feed_source_category_subscription_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
