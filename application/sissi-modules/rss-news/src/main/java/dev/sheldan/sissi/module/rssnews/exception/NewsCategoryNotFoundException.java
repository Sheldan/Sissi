package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class NewsCategoryNotFoundException extends AbstractoTemplatableException {

    public NewsCategoryNotFoundException() {
        super("News Category not found.");
    }

    @Override
    public String getTemplateName() {
        return "news_category_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
