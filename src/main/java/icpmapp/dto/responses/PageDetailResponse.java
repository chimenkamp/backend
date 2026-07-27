package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDetailResponse {
    private UUID id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary createdBy;
    private UserSummary updatedBy;
    private List<PageMessageResponse> messages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private Integer id;
        private String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageMessageResponse {
        private UUID id;
        private String content;
        private Integer sortOrder;
        private LocalDateTime createdAt;
    }
}
