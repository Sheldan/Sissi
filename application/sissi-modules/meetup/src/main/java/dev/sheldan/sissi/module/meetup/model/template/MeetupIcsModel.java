package dev.sheldan.sissi.module.meetup.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Builder
@Setter
public class MeetupIcsModel {
    private Boolean attachIcsFile;
    private String iceFileName;
    private String icsFormattedCreationTime;
    private String icsFormattedStartTime;
    private String icsFormattedEndTime;
}
