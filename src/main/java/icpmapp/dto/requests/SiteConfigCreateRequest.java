package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfigCreateRequest {
    private String key;
    private String value;
    private String type;
    private String category;
    private String description;
    private Boolean isPublic;
}
