package dev.sheldan.sissi.module.meetup.repository;

import dev.sheldan.sissi.module.meetup.model.database.MeetupComponent;
import dev.sheldan.sissi.module.meetup.model.database.embed.MeetupComponentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetupComponentRepository extends JpaRepository<MeetupComponent, MeetupComponentId> {
}
