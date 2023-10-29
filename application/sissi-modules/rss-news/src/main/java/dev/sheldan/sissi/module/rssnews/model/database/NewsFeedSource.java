package dev.sheldan.sissi.module.rssnews.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_feed_source")
@Getter
@Setter
@EqualsAndHashCode
public class NewsFeedSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "feed_type", nullable = false)
    private NewsFeedSourceCategoryType type;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "source")
    @Builder.Default
    private List<NewsFeedSourceCategory> categories = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "source")
    @Builder.Default
    private List<NewsFeedRecord> records = new ArrayList<>();

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
