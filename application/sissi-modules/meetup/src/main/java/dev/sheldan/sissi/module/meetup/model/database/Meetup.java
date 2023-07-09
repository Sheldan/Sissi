package dev.sheldan.sissi.module.meetup.model.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetup")
@Getter
@Setter
@EqualsAndHashCode
public class Meetup {

    @EmbeddedId
    @Getter
    @Id
    private ServerSpecificId id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_user_in_server_id", nullable = false)
    private AUserInAServer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_channel_id", nullable = false)
    private AChannel meetupChannel;

    @Getter
    @Column(name = "message_id")
    private Long messageId;

    @Getter
    @Column(name = "location")
    private String location;

    @Getter
    @Column(name = "yes_button_id")
    private String yesButtonId;

    @Getter
    @Column(name = "maybe_button_id")
    private String maybeButtonId;

    @Getter
    @Column(name = "no_time_button_id")
    private String noTimeButtonId;

    @Getter
    @Column(name = "not_interested_button_id")
    private String notInterestedButtonId;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "meetup")
    @Builder.Default
    private List<MeetupParticipant> participants = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "meetup")
    @Builder.Default
    private List<MeetupComponent> meetupComponents = new ArrayList<>();

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private MeetupState state;

    @Getter
    @Column(name = "topic", nullable = false)
    private String topic;

    @Getter
    @Column(name = "description")
    private String description;

    @Column(name = "meetup_time", nullable = false)
    private Instant meetupTime;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    @Column(name = "early_reminder_job_trigger_key")
    private String earlyReminderJobTriggerKey;

    @Column(name = "late_reminder_job_trigger_key")
    private String lateReminderJobTriggerKey;
}
