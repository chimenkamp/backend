package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationConfigDetailResponse {
    private UUID id;
    private String tabKey;
    private String labelKey;
    private String icon;
    private String route;
    private Integer sortOrder;
    private Boolean isEnabled;
    private String requiredRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
