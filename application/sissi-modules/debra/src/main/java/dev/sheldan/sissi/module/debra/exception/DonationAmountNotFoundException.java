package dev.sheldan.sissi.module.debra.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class DonationAmountNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "donation_amount_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
