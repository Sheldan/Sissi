package dev.sheldan.sissi.module.meetup.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipator;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupParticipationId;
import dev.sheldan.sissi.module.meetup.repository.MeetupParticipatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MeetupParticipatorManagementServiceBean {

    @Autowired
    private MeetupParticipatorRepository repository;

    public Optional<MeetupParticipator> getParticipation(Meetup meetup, AUserInAServer aUserInAServer) {
        MeetupParticipationId id = MeetupParticipationId
                .builder()
                .meetupId(meetup.getId().getId())
                .serverId(meetup.getServer().getId())
                .participatorId(aUserInAServer.getUserInServerId())
                .build();
        return repository.findById(id);
    }

    public MeetupParticipator createParticipation(Meetup meetup, AUserInAServer aUserInAServer, MeetupDecision meetupDecision) {
        MeetupParticipationId id = MeetupParticipationId
                .builder()
                .meetupId(meetup.getId().getId())
                .serverId(meetup.getServer().getId())
                .participatorId(aUserInAServer.getUserInServerId())
                .build();
        MeetupParticipator participator = MeetupParticipator
                .builder()
                .id(id)
                .meetup(meetup)
                .participator(aUserInAServer)
                .decision(meetupDecision)
                .build();
        return repository.save(participator);
    }
}
