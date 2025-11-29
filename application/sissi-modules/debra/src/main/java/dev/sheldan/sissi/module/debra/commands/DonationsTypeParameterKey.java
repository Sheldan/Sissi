package dev.sheldan.sissi.module.debra.commands;

import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DonationsTypeParameterKey implements CommandParameterKey {
    TOP("top"), LATEST("latest");

    private String key;
}
