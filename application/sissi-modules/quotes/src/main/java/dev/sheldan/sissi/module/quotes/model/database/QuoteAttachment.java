package dev.sheldan.sissi.module.quotes.model.database;

import lombok.*;

import javax.persistence.*;

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
    @Setter
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "quote_id", referencedColumnName = "id"),
                    @JoinColumn(updatable = false, insertable = false, name = "server_id", referencedColumnName = "server_id")
            })
    private Quote quote;

    @Getter
    @Setter
    @Column(name = "url", nullable = false)
    private String url;

    @Getter
    @Setter
    @Column(name = "is_image", nullable = false)
    @Builder.Default
    private Boolean isImage = false;

}
