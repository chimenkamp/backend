package icpmapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table(name = "localized_content", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"content_key", "language_code"})
})
@Entity
public class LocalizedContent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_key", nullable = false, length = 255)
    private String contentKey;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Lob
    @Column(name = "content_value", nullable = false, columnDefinition = "TEXT")
    private String contentValue;

    @Column(name = "content_type", length = 20)
    private String contentType = "text";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public LocalizedContent(String contentKey, String languageCode, String contentValue, String contentType) {
        this.contentKey = contentKey;
        this.languageCode = languageCode;
        this.contentValue = contentValue;
        this.contentType = contentType;
    }
}
