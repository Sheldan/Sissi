package dev.sheldan.sissi.module.rssnews.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsCategorySubscriptionInfo {
    private String newsFeedName;
    private List<String> newsFeedCategories;
}
