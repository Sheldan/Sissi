package dev.sheldan.sissi.module.rssnews.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_post")
@Getter
@Setter
@EqualsAndHashCode
public class NewsPost {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "url")
    private String url;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", referencedColumnName = "id", nullable = false)
    private AChannel postChannel;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "news_category_channel_mapping_id", referencedColumnName = "id", nullable = false)
    private NewsCategoryChannelMapping mapping;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
