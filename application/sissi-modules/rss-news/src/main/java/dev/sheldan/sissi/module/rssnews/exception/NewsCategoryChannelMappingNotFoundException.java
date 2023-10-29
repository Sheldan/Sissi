package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class NewsCategoryChannelMappingNotFoundException extends AbstractoTemplatableException {

    public NewsCategoryChannelMappingNotFoundException() {
        super("News Category channel mapping not found.");
    }

    @Override
    public String getTemplateName() {
        return "news_category_channel_mapping_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
