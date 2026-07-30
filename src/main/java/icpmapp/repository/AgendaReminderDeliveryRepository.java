package icpmapp.repository;

import icpmapp.entities.AgendaReminderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaReminderDeliveryRepository extends JpaRepository<AgendaReminderDelivery, Long> {
    boolean existsBySubscription_IdAndSession_Id(Long subscriptionId, Long sessionId);

    void deleteBySubscription_Id(Long subscriptionId);
}
