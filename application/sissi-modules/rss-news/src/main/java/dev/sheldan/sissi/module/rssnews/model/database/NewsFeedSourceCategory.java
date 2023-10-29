package dev.sheldan.sissi.module.rssnews.model.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "news_feed_source_category")
@Getter
@Setter
@EqualsAndHashCode
public class NewsFeedSourceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    private NewsFeedSource source;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    @Getter
    @ManyToMany(mappedBy = "sourceCategories")
    private List<NewsCategory> categories;

}
