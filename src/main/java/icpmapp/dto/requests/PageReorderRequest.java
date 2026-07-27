package icpmapp.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageReorderRequest {
    private List<PageOrderItem> pages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageOrderItem {
        private UUID id;
        private Integer sortOrder;
    }
}
