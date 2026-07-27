package icpmapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "content")
public class Content {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "content_key", nullable = false, unique = true)
    private String key;

    @Lob
    @Column(name = "content_value", columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentCategory category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Content() {
    }

    public Content(String key, String value, ContentType type, ContentCategory category, String updatedBy) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.category = category;
        this.updatedBy = updatedBy;
    }
}
