package dev.sheldan.sissi.module.miepscord.weeklytext.model.template;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ShowTextItemModel {
    private String text;
    private Boolean done;
    private Instant created;
    private Long id;
}
