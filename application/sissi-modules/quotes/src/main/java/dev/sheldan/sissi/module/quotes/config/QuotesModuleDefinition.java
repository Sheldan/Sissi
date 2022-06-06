package dev.sheldan.sissi.module.quotes.config;

import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class QuotesModuleDefinition implements ModuleDefinition {

    public static final String QUOTES = "quotes";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo
                .builder()
                .name(QUOTES)
                .templated(true)
                .build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
