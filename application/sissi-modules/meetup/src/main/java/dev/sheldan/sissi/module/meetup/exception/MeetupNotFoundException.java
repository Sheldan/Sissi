package dev.sheldan.sissi.module.meetup.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class MeetupNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "meetup_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
