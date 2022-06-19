package dev.sheldan.sissi.module.meetup.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class MeetupPastTimeException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "meetup_in_past_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
