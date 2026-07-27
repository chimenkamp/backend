package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NavigationItemResponse {
    private String key;
    private String labelKey;
    private String icon;
    private String route;
    private Boolean enabled;
    private String requiredRole;
    private Integer sortOrder;
}
