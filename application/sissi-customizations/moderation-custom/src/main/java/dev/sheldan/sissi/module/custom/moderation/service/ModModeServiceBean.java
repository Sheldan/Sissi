package dev.sheldan.sissi.module.custom.moderation.service;

import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.sissi.module.custom.moderation.exception.ModRoleNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ModModeServiceBean {
    public static final String MODMODE_ROLE_CONFIG_KEY = "modModeRoleId";
    public static final String MODMODE_CHANGED_ROLE_COLOR_CONFIG_KEY = "modModeNewRoleColor";

    @Autowired
    private ConfigService configService;

    public CompletableFuture<Void> setModModeTo(Guild guild, Boolean newState) {
        if(Boolean.TRUE.equals(newState)) {
            return enableModMode(guild);
        } else {
            return disableModMoe(guild);
        }
    }

    private CompletableFuture<Void> enableModMode(Guild guild) {
        Color colorToSet = getColorFromConfig(MODMODE_CHANGED_ROLE_COLOR_CONFIG_KEY, guild);
        return setModRoleTo(guild, colorToSet);
    }

    private CompletableFuture<Void> disableModMoe(Guild guild) {
        return setModRoleTo(guild, null);
    }

    private Color getColorFromConfig(String key, Guild guild) {
        String colorString = configService.getStringValueOrConfigDefault(key, guild.getIdLong());
        String[] parts = colorString.split(",");
        return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private CompletableFuture<Void> setModRoleTo(Guild guild, Color color) {
        Long roleId = configService.getLongValue(MODMODE_ROLE_CONFIG_KEY, guild.getIdLong());
        Role modRole = guild.getRoleById(roleId);
        if(modRole != null) {
            return modRole.getManager().setColor(color).submit();
        } else {
            throw new ModRoleNotFoundException();
        }
    }
}
