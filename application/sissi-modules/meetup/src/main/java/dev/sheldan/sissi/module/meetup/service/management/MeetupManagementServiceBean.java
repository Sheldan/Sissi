package dev.sheldan.sissi.module.meetup.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.CounterService;
import dev.sheldan.sissi.module.meetup.exception.MeetupNotFoundException;
import dev.sheldan.sissi.module.meetup.exception.MeetupPastTimeException;
import dev.sheldan.sissi.module.meetup.model.database.Meetup;
import dev.sheldan.sissi.module.meetup.model.database.MeetupState;
import dev.sheldan.sissi.module.meetup.repository.MeetupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class MeetupManagementServiceBean {

    @Autowired
    private MeetupRepository meetupRepository;

    @Autowired
    private CounterService counterService;

    public static final String MEETUP_COUNTER_KEY = "meetup";

    public Meetup createMeetup(Instant timeStamp, String topic, String description, AUserInAServer organizer, AChannel meetupChannel, String location) {
        if(timeStamp.isBefore(Instant.now())) {
            throw new MeetupPastTimeException();
        }
        Long meetupId = counterService.getNextCounterValue(organizer.getServerReference(), MEETUP_COUNTER_KEY);
        Meetup meetup = null;
        try {
            meetup = Meetup
                    .builder()
                    .meetupTime(timeStamp)
                    .description(description)
                    .topic(topic)
                    .location(URLEncoder.encode(location, StandardCharsets.UTF_8.toString()))
                    .organizer(organizer)
                    .server(organizer.getServerReference())
                    .meetupChannel(meetupChannel)
                    .state(MeetupState.NEW)
                    .id(new ServerSpecificId(organizer.getServerReference().getId(), meetupId))
                    .build();
        } catch (UnsupportedEncodingException e) {
            throw new AbstractoRunTimeException(e);
        }
        return meetupRepository.save(meetup);
    }

    public Meetup getMeetup(Long meetupId, Long serverId) {
        return meetupRepository.findById(new ServerSpecificId(serverId, meetupId))
                .orElseThrow(MeetupNotFoundException::new);
    }

    public List<Meetup> getMeetupsOlderThan(Instant instant) {
        return meetupRepository.findByMeetupTimeLessThan(instant);
    }

    public List<Meetup> getMeetups() {
        return meetupRepository.findAll();
    }

    public List<Meetup> findCancelledMeetups() {
        return meetupRepository.findByState(MeetupState.CANCELLED);
    }

    public List<Meetup> getIncomingMeetups(Long serverId, Long channelId) {
        return meetupRepository.findByMeetupTimeGreaterThanAndStateAndServer_IdAndMeetupChannel_Id(Instant.now(), MeetupState.CONFIRMED, serverId, channelId);
    }

    public void deleteMeetups(List<Meetup> meetups) {
        meetupRepository.deleteAll(meetups);
    }
    public void deleteMeetup(Meetup meetup) {
        meetupRepository.delete(meetup);
    }
}
