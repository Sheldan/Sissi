package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsCategoryChannelMappingAlreadyExistsException extends AbstractoTemplatableException {

    public NewsCategoryChannelMappingAlreadyExistsException() {
        super("News Category channel mapping already exists.");
    }

    @Override
    public String getTemplateName() {
        return "news_category_channel_mapping_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
