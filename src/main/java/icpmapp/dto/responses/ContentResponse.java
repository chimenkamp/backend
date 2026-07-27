package icpmapp.dto.responses;

import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    private String id;
    private String key;
    private String value;
    private ContentType type;
    private ContentCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
