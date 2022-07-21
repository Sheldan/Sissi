package dev.sheldan.sissi.module.custom.moderation.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class ModRoleNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "mod_role_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
