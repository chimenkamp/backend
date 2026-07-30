package icpmapp.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import icpmapp.config.AgendaReminderProperties;
import icpmapp.dto.responses.PushReminderPayload;
import icpmapp.entities.AgendaPushSubscription;
import icpmapp.services.PushNotificationSender;
import icpmapp.services.PushSendResult;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Urgency;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.security.Security;

@Component
public class WebPushNotificationSender implements PushNotificationSender {
    private final AgendaReminderProperties properties;
    private final ObjectMapper objectMapper;

    public WebPushNotificationSender(
        AgendaReminderProperties properties,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public PushSendResult send(
        AgendaPushSubscription subscription,
        PushReminderPayload payload
    ) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Web Push is not configured");
        }

        try {
            PushService pushService = new PushService(
                properties.getPublicKey(),
                properties.getPrivateKey(),
                properties.getSubject()
            );
            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                serialize(payload),
                Urgency.HIGH
            );
            HttpResponse response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404 || status == 410) {
                return PushSendResult.EXPIRED;
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Push service returned HTTP " + status);
            }
            return PushSendResult.DELIVERED;
        } catch (Exception error) {
            throw new IllegalStateException("Could not send Web Push notification", error);
        }
    }

    private String serialize(PushReminderPayload payload) throws JsonProcessingException {
        return objectMapper.writeValueAsString(payload);
    }
}
