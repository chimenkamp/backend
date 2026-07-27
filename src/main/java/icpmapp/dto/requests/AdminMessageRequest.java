package icpmapp.dto.requests;

import icpmapp.entities.MessagePriority;
import icpmapp.entities.RecipientType;
import lombok.Data;

import java.util.List;

@Data
public class AdminMessageRequest {
    private RecipientType recipientType;
    private List<Integer> recipients;
    private String title;
    private String message;
    private MessagePriority priority = MessagePriority.NORMAL;
    private Boolean sendPush = false;
    private Boolean sendEmail = false;
}
