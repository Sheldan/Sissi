package dev.sheldan.sissi.module.rssnews.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_feed_record")
@Getter
@Setter
@EqualsAndHashCode
public class NewsFeedRecord {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    private NewsFeedSource source;

    @Column(name = "url")
    private String url;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
