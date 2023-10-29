package dev.sheldan.sissi.module.rssnews.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_category")
@Getter
@Setter
@EqualsAndHashCode
public class NewsCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @ManyToMany
    @JoinTable(
            name = "news_category_in_news_feed_source_category",
            joinColumns = @JoinColumn(name = "server_category_id"),
            inverseJoinColumns = @JoinColumn(name = "source_category_id"))
    private List<NewsFeedSourceCategory> sourceCategories;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "newsCategory")
    @Builder.Default
    private List<NewsCategoryChannelMapping> mappings = new ArrayList<>();

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
