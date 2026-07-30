package icpmapp.services;

import icpmapp.dto.responses.PushReminderPayload;
import icpmapp.entities.AgendaPushSubscription;

public interface PushNotificationSender {
    PushSendResult send(AgendaPushSubscription subscription, PushReminderPayload payload);
}
