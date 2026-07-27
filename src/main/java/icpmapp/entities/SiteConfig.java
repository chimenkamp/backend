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
@Table(name = "site_config")
@Entity
public class SiteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "config_key", unique = true, nullable = false, length = 255)
    private String configKey;

    @Lob
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_type", nullable = false, length = 20)
    private String configType = "text";

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public SiteConfig(String configKey, String configValue, String configType, String category, Boolean isPublic) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.category = category;
        this.isPublic = isPublic;
    }

    public SiteConfig(String configKey, String configValue, String configType, String category, String description, Boolean isPublic) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.category = category;
        this.description = description;
        this.isPublic = isPublic;
    }
}
