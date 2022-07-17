package dev.sheldan.sissi.module.meetup.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupDecision;
import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipant;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupParticipationId;
import dev.sheldan.sissi.module.meetup.repository.MeetupParticipatorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MeetupParticipatorManagementServiceBean {

    @Autowired
    private MeetupParticipatorRepository repository;

    public Optional<MeetupParticipant> getParticipation(Meetup meetup, AUserInAServer aUserInAServer) {
        MeetupParticipationId id = MeetupParticipationId
                .builder()
                .meetupId(meetup.getId().getId())
                .serverId(meetup.getServer().getId())
                .participatorId(aUserInAServer.getUserInServerId())
                .build();
        return repository.findById(id);
    }

    public void deleteParticipants(List<MeetupParticipant> participants) {
        log.info("Deleting {} participants", participants.size());
        repository.deleteAll(participants);
        participants.forEach(meetupParticipant -> meetupParticipant.setMeetup(null));
    }

    public MeetupParticipant createParticipation(Meetup meetup, AUserInAServer aUserInAServer, MeetupDecision meetupDecision) {
        MeetupParticipationId id = MeetupParticipationId
                .builder()
                .meetupId(meetup.getId().getId())
                .serverId(meetup.getServer().getId())
                .participatorId(aUserInAServer.getUserInServerId())
                .build();
        MeetupParticipant participator = MeetupParticipant
                .builder()
                .id(id)
                .meetup(meetup)
                .participator(aUserInAServer)
                .decision(meetupDecision)
                .build();
        return repository.save(participator);
    }
}
