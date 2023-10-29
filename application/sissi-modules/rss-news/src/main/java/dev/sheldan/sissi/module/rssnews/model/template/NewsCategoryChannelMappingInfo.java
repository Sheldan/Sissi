package dev.sheldan.sissi.module.rssnews.model.template;

import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewsCategoryChannelMappingInfo {
    private ChannelDisplay channel;
    private Boolean enabled;
}
