package dev.sheldan.sissi.module.quotes.model.database;

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
@Table(name = "quote")
@Getter
@Setter
@EqualsAndHashCode
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_in_server_id", nullable = false)
    private AUserInAServer author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adder_user_in_server_id", nullable = false)
    private AUserInAServer adder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_channel_id", nullable = false)
    private AChannel sourceChannel;

    @Getter
    @Column(name = "message_id")
    private Long messageId;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "quote")
    @Builder.Default
    private List<QuoteAttachment> attachments = new ArrayList<>();

    @Getter
    @Column(name = "text")
    private String text;

    @Column(name = "created", insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
