package dev.sheldan.sissi.module.rssnews.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;


public class NewsCategoryAlreadyExistsException extends AbstractoTemplatableException {

    public NewsCategoryAlreadyExistsException() {
        super("News Category already exists.");
    }

    @Override
    public String getTemplateName() {
        return "news_category_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
