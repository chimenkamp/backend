package icpmapp.services.impl;

import icpmapp.config.AgendaReminderProperties;
import icpmapp.dto.requests.AgendaPushSubscriptionRequest;
import icpmapp.dto.responses.PushReminderConfigResponse;
import icpmapp.dto.responses.PushReminderPayload;
import icpmapp.entities.AgendaPushSubscription;
import icpmapp.entities.AgendaReminderDelivery;
import icpmapp.entities.SessionHeader;
import icpmapp.entities.User;
import icpmapp.repository.AgendaPushSubscriptionRepository;
import icpmapp.repository.AgendaReminderDeliveryRepository;
import icpmapp.repository.SessionHeaderRepository;
import icpmapp.repository.UserRepository;
import icpmapp.services.AgendaReminderService;
import icpmapp.services.PushNotificationSender;
import icpmapp.services.PushSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaReminderServiceImpl implements AgendaReminderService {
    private final AgendaPushSubscriptionRepository subscriptions;
    private final AgendaReminderDeliveryRepository deliveries;
    private final SessionHeaderRepository sessions;
    private final UserRepository users;
    private final PushNotificationSender sender;
    private final AgendaReminderProperties properties;

    @Override
    public PushReminderConfigResponse getConfig() {
        return new PushReminderConfigResponse(
            properties.isConfigured(),
            properties.isConfigured() ? properties.getPublicKey() : "",
            properties.getMinutesBefore()
        );
    }

    @Override
    @Transactional
    public void subscribe(String username, AgendaPushSubscriptionRequest request) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Web Push reminders are not configured"
            );
        }
        validateEndpoint(request.endpoint());
        User user = users.findByEmail(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        AgendaPushSubscription subscription = subscriptions
            .findByEndpoint(request.endpoint())
            .orElseGet(AgendaPushSubscription::new);
        if (subscription.getId() != null
            && !Objects.equals(subscription.getUser().getId(), user.getId())) {
            deliveries.deleteBySubscription_Id(subscription.getId());
        }
        subscription.setUser(user);
        subscription.setEndpoint(request.endpoint());
        subscription.setP256dh(request.p256dh());
        subscription.setAuth(request.auth());
        subscription.setLocale(normalizeLocale(request.locale()));
        subscriptions.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(String username, String endpoint) {
        subscriptions.findByEndpointAndUser_Email(endpoint, username)
            .ifPresent(subscriptions::delete);
    }

    @Scheduled(fixedDelayString = "${conferia.reminders.poll-ms:30000}")
    public void dispatchDueReminders() {
        dispatchDueReminders(LocalDateTime.now());
    }

    @Override
    public void dispatchDueReminders(LocalDateTime now) {
        if (!properties.isConfigured()) {
            return;
        }
        LocalDateTime from = now.truncatedTo(ChronoUnit.MINUTES)
            .plusMinutes(properties.getMinutesBefore());
        LocalDateTime to = from.plusMinutes(1);

        for (AgendaPushSubscription subscription : subscriptions.findAllWithUser()) {
            for (SessionHeader session : sessions.findUpcomingLikedSessions(
                subscription.getUser().getId(),
                from,
                to
            )) {
                if (deliveries.existsBySubscription_IdAndSession_Id(
                    subscription.getId(),
                    session.getId()
                )) {
                    continue;
                }
                try {
                    PushSendResult result = sender.send(
                        subscription,
                        createPayload(subscription, session)
                    );
                    if (result == PushSendResult.EXPIRED) {
                        subscriptions.delete(subscription);
                        break;
                    }
                    AgendaReminderDelivery delivery = new AgendaReminderDelivery();
                    delivery.setSubscription(subscription);
                    delivery.setSession(session);
                    delivery.setDeliveredAt(now);
                    deliveries.save(delivery);
                } catch (RuntimeException error) {
                    log.error(
                        "Could not send reminder for session {} to subscription {}",
                        session.getId(),
                        subscription.getId(),
                        error
                    );
                }
            }
        }
    }

    private PushReminderPayload createPayload(
        AgendaPushSubscription subscription,
        SessionHeader session
    ) {
        boolean german = subscription.getLocale().toLowerCase(Locale.ROOT).startsWith("de");
        int minutes = properties.getMinutesBefore();
        String title = german
            ? "Beginnt in " + minutes + " Minuten"
            : "Starts in " + minutes + " minutes";
        String location = session.getLocation() == null ? "" : session.getLocation();
        String body = location.isBlank()
            ? session.getName()
            : session.getName() + " · " + location;
        String date = session.getStartTime().toLocalDate().toString();
        return new PushReminderPayload(
            session.getId(),
            session.getName(),
            location,
            session.getStartTime().toString(),
            minutes,
            title,
            body,
            "/#/tabs/agenda?date=" + date,
            "agenda-session-" + session.getId()
        );
    }

    private void validateEndpoint(String endpoint) {
        URI uri;
        try {
            uri = URI.create(endpoint);
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid push endpoint");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Push endpoint must use HTTPS"
            );
        }
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        return locale.trim().toLowerCase(Locale.ROOT);
    }
}
