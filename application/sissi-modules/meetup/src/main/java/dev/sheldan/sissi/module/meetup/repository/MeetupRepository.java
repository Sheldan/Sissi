package dev.sheldan.sissi.module.meetup.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MeetupRepository extends JpaRepository<Meetup, ServerSpecificId> {
    List<Meetup> findByMeetupTimeLessThan(Instant date);
    List<Meetup> findByMeetupTimeGreaterThan(Instant date);
    List<Meetup> findByState(MeetupState state);
}
