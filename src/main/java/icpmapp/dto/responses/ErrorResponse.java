package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String error;
    private String code;
    private Map<String, Object> details;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String error, String code) {
        this.error = error;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
}
