package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private String title;
    private String slug;
    private String content;
    private Integer layoutId;
    private Integer sortOrder;
    private Boolean isPublished;
    private Boolean isPublic;
    private String label;
    private String labelColor;
    private String icon;
}

