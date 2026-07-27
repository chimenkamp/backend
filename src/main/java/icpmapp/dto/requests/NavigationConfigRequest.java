package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavigationConfigRequest {
    private String labelKey;
    private String icon;
    private String route;
    private Integer sortOrder;
    private Boolean isEnabled;
    private String requiredRole;
}
