package icpmapp.dto.requests;

import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;
import lombok.Data;

@Data
public class UpdateContentRequest {
    private String value;
    private ContentType type;
    private ContentCategory category;
}
