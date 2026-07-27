package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavigationReorderRequest {
    private List<NavigationOrderItem> navigation;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NavigationOrderItem {
        private UUID id;           // Optional - not used for lookup
        private String tabKey;     // Required - used as unique identifier
        private String labelKey;
        private String icon;
        private String route;
        private Integer sortOrder;
        private Boolean isEnabled;
        private String requiredRole;
    }
}
