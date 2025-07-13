package dev.sheldan.sissi.module.quotes.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import jakarta.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quote_attachment")
@Getter
@Setter
@EqualsAndHashCode
public class QuoteAttachment {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, name = "quote_id", referencedColumnName = "id")
            })
    private Quote quote;

    @Getter
    @Setter
    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @Setter
    @Column(name = "is_media", nullable = false)
    @Builder.Default
    private Boolean isMedia = false;

}
