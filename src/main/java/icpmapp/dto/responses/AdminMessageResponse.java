package icpmapp.dto.responses;

import icpmapp.entities.MessagePriority;
import icpmapp.entities.RecipientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageResponse {
    private String id;
    private String title;
    private String message;
    private MessagePriority priority;
    private RecipientType recipientType;
    private List<Integer> recipients;
    private Integer recipientCount;
    private Integer readCount;
    private Boolean pushSent;
    private Boolean emailSent;
    private LocalDateTime sentAt;
    private String sentBy;
}
