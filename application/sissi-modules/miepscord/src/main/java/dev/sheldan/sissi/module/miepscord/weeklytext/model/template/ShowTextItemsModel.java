package dev.sheldan.sissi.module.miepscord.weeklytext.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ShowTextItemsModel {
    private List<ShowTextItemModel> items;
}
