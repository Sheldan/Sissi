package dev.sheldan.sissi.module.rssnews.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsCategoryInfo {
    private String name;
    private List<NewsCategorySubscriptionInfo> subscriptions;
    private List<NewsCategoryChannelMappingInfo> mappings;
}
