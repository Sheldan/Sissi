package dev.sheldan.sissi.module.rssnews.model.feed.rfd;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RFDItem {
    private String title;
    private String link;
    private String subject;
    private String date;
    private String oewaCategory;
    private String description;
}
