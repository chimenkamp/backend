package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfigBulkRequest {
    private List<ConfigItem> configs;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigItem {
        private String key;
        private String value;
    }
}
