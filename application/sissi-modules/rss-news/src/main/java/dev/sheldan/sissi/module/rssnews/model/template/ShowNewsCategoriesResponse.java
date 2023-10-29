package dev.sheldan.sissi.module.rssnews.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ShowNewsCategoriesResponse {
    private List<NewsCategoryInfo> newsCategories;
}
