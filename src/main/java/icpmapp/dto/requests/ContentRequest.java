package icpmapp.dto.requests;

import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;
import lombok.Data;

@Data
public class ContentRequest {
    private String key;
    private String value;
    private ContentType type;
    private ContentCategory category;
}
