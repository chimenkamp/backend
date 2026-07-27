package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageSentResponse {
    private String id;
    private String title;
    private Integer recipientCount;
    private Boolean pushSent;
    private Boolean emailSent;
    private LocalDateTime sentAt;
}
