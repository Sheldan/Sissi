package dev.sheldan.sissi.module.meetup.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MeetupListModel {
    private List<MeetupListItemModel> meetups;
}
