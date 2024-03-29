package dev.sheldan.sissi.module.meetup.repository;

import dev.sheldan.sissi.module.meetup.model.database.MeetupParticipant;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetupParticipatorRepository extends JpaRepository<MeetupParticipant, MeetupParticipationId> {
}
