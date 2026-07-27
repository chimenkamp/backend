package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageListResponse {
    private UUID id;
    private String title;
    private String slug;
    private Integer layoutId;
    private Integer sortOrder;
    private String label;
    private String labelColor;
    private String icon;
}
