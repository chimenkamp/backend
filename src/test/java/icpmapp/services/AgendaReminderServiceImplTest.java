package icpmapp.services;

import icpmapp.config.AgendaReminderProperties;
import icpmapp.dto.requests.AgendaPushSubscriptionRequest;
import icpmapp.dto.responses.PushReminderPayload;
import icpmapp.entities.AgendaPushSubscription;
import icpmapp.entities.SessionHeader;
import icpmapp.entities.User;
import icpmapp.repository.AgendaPushSubscriptionRepository;
import icpmapp.repository.AgendaReminderDeliveryRepository;
import icpmapp.repository.SessionHeaderRepository;
import icpmapp.repository.UserRepository;
import icpmapp.services.impl.AgendaReminderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgendaReminderServiceImplTest {

    @Test
    void sendsOneReminderFifteenMinutesBeforeEachFavoriteSession() throws Exception {
        AgendaPushSubscriptionRepository subscriptions = mock(AgendaPushSubscriptionRepository.class);
        AgendaReminderDeliveryRepository deliveries = mock(AgendaReminderDeliveryRepository.class);
        SessionHeaderRepository sessions = mock(SessionHeaderRepository.class);
        PushNotificationSender sender = mock(PushNotificationSender.class);
        AgendaReminderProperties properties = configuredProperties();
        AgendaReminderServiceImpl service = new AgendaReminderServiceImpl(
            subscriptions,
            deliveries,
            sessions,
            mock(UserRepository.class),
            sender,
            properties
        );
        User user = new User();
        user.setId(4);
        AgendaPushSubscription subscription = new AgendaPushSubscription();
        subscription.setId(3L);
        subscription.setUser(user);
        subscription.setLocale("en");
        SessionHeader session = new SessionHeader();
        session.setId(8L);
        session.setName("Opening keynote");
        session.setLocation("Main Hall");
        session.setStartTime(LocalDateTime.of(2026, 7, 27, 12, 15));
        when(subscriptions.findAllWithUser()).thenReturn(List.of(subscription));
        when(sessions.findUpcomingLikedSessions(
            4,
            LocalDateTime.of(2026, 7, 27, 12, 15),
            LocalDateTime.of(2026, 7, 27, 12, 16)
        )).thenReturn(List.of(session));
        when(deliveries.existsBySubscription_IdAndSession_Id(3L, 8L)).thenReturn(false);
        when(sender.send(any(), any())).thenReturn(PushSendResult.DELIVERED);

        service.dispatchDueReminders(LocalDateTime.of(2026, 7, 27, 12, 0, 47));

        ArgumentCaptor<PushReminderPayload> payload = ArgumentCaptor.forClass(PushReminderPayload.class);
        verify(sender).send(eq(subscription), payload.capture());
        assertEquals("Opening keynote", payload.getValue().sessionName());
        assertEquals(15, payload.getValue().minutesBefore());
        verify(deliveries).save(any());
    }

    @Test
    void doesNothingWhenTheUserHasNotOptedIn() {
        AgendaPushSubscriptionRepository subscriptions = mock(AgendaPushSubscriptionRepository.class);
        PushNotificationSender sender = mock(PushNotificationSender.class);
        when(subscriptions.findAllWithUser()).thenReturn(List.of());
        AgendaReminderServiceImpl service = new AgendaReminderServiceImpl(
            subscriptions,
            mock(AgendaReminderDeliveryRepository.class),
            mock(SessionHeaderRepository.class),
            mock(UserRepository.class),
            sender,
            configuredProperties()
        );

        service.dispatchDueReminders(LocalDateTime.of(2026, 7, 27, 12, 0));

        verify(sender, never()).send(any(), any());
    }

    @Test
    void removesExpiredBrowserSubscriptions() throws Exception {
        AgendaPushSubscriptionRepository subscriptions = mock(AgendaPushSubscriptionRepository.class);
        AgendaReminderDeliveryRepository deliveries = mock(AgendaReminderDeliveryRepository.class);
        SessionHeaderRepository sessions = mock(SessionHeaderRepository.class);
        PushNotificationSender sender = mock(PushNotificationSender.class);
        AgendaReminderServiceImpl service = new AgendaReminderServiceImpl(
            subscriptions,
            deliveries,
            sessions,
            mock(UserRepository.class),
            sender,
            configuredProperties()
        );
        User user = new User();
        user.setId(4);
        AgendaPushSubscription subscription = new AgendaPushSubscription();
        subscription.setId(3L);
        subscription.setUser(user);
        SessionHeader session = new SessionHeader();
        session.setId(8L);
        session.setStartTime(LocalDateTime.of(2026, 7, 27, 12, 15));
        when(subscriptions.findAllWithUser()).thenReturn(List.of(subscription));
        when(sessions.findUpcomingLikedSessions(any(), any(), any())).thenReturn(List.of(session));
        when(sender.send(any(), any())).thenReturn(PushSendResult.EXPIRED);

        service.dispatchDueReminders(LocalDateTime.of(2026, 7, 27, 12, 0));

        verify(subscriptions).delete(subscription);
        verify(deliveries, never()).save(any());
    }

    @Test
    void clearsOldDeliveriesWhenABrowserSubscriptionMovesToAnotherUser() {
        AgendaPushSubscriptionRepository subscriptions = mock(AgendaPushSubscriptionRepository.class);
        AgendaReminderDeliveryRepository deliveries = mock(AgendaReminderDeliveryRepository.class);
        UserRepository users = mock(UserRepository.class);
        AgendaPushSubscription existing = new AgendaPushSubscription();
        existing.setId(3L);
        User previousUser = new User();
        previousUser.setId(4);
        existing.setUser(previousUser);
        User currentUser = new User();
        currentUser.setId(7);
        when(subscriptions.findByEndpoint("https://push.example/subscription"))
            .thenReturn(Optional.of(existing));
        when(users.findByEmail("current@example.com")).thenReturn(Optional.of(currentUser));
        AgendaReminderServiceImpl service = new AgendaReminderServiceImpl(
            subscriptions,
            deliveries,
            mock(SessionHeaderRepository.class),
            users,
            mock(PushNotificationSender.class),
            configuredProperties()
        );

        service.subscribe(
            "current@example.com",
            new AgendaPushSubscriptionRequest(
                "https://push.example/subscription",
                "p256dh",
                "auth",
                "en"
            )
        );

        verify(deliveries).deleteBySubscription_Id(3L);
        assertEquals(currentUser, existing.getUser());
        verify(subscriptions).save(existing);
    }

    private AgendaReminderProperties configuredProperties() {
        AgendaReminderProperties properties = new AgendaReminderProperties();
        properties.setPublicKey("public");
        properties.setPrivateKey("private");
        properties.setSubject("mailto:admin@example.com");
        properties.setMinutesBefore(15);
        assertTrue(properties.isConfigured());
        return properties;
    }
}
