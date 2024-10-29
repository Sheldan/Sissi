package dev.sheldan.sissi.module.miepscord.weeklytext.model.template;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class TextItemPostModel {
    private String text;
    private Long id;
    private Instant created;
}
