package icpmapp.services;

import icpmapp.dto.requests.AgendaPushSubscriptionRequest;
import icpmapp.dto.responses.PushReminderConfigResponse;

import java.time.LocalDateTime;

public interface AgendaReminderService {
    PushReminderConfigResponse getConfig();

    void subscribe(String username, AgendaPushSubscriptionRequest request);

    void unsubscribe(String username, String endpoint);

    void dispatchDueReminders(LocalDateTime now);
}
