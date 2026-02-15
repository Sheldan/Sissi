package dev.sheldan.sissi.module.meetup.service.management;

import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupComponent;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupComponentId;
import dev.sheldan.sissi.module.meetup.repository.MeetupComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MeetupComponentManagementServiceBean {

    @Autowired
    private MeetupComponentRepository meetupComponentRepository;

    public MeetupComponent createComponent(Meetup meetup, String componentId, ComponentPayload payload) {
        MeetupComponentId id = MeetupComponentId
                .builder()
                .meetupId(meetup.getId().getId())
                .serverId(meetup.getServer().getId())
                .componentId(componentId)
                .build();
        MeetupComponent component = MeetupComponent
                .builder()
                .id(id)
                .payload(payload)
                .meetup(meetup)
                .build();
        return meetupComponentRepository.save(component);
    }

    public void deleteAllComponents(Meetup meetup) {
        meetupComponentRepository.deleteAll(meetup.getMeetupComponents());
    }
}
