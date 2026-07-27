package icpmapp.dto.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {
    private String name;
    private String host;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String content;
    private String track;
    private Boolean isPublished = false;
}
