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
@Table(name = "navigation_config")
@Entity
public class NavigationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tab_key", unique = true, nullable = false, length = 50)
    private String tabKey;

    @Column(name = "label_key", nullable = false, length = 100)
    private String labelKey;

    @Column(nullable = false, length = 100)
    private String icon;

    @Column(nullable = false, length = 255)
    private String route;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "required_role", length = 50)
    private String requiredRole;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NavigationConfig(String tabKey, String labelKey, String icon, String route, Integer sortOrder) {
        this.tabKey = tabKey;
        this.labelKey = labelKey;
        this.icon = icon;
        this.route = route;
        this.sortOrder = sortOrder;
    }

    public NavigationConfig(String tabKey, String labelKey, String icon, String route, Integer sortOrder, String requiredRole) {
        this.tabKey = tabKey;
        this.labelKey = labelKey;
        this.icon = icon;
        this.route = route;
        this.sortOrder = sortOrder;
        this.requiredRole = requiredRole;
    }
}
