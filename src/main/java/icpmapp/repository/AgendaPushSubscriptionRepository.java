package icpmapp.repository;

import icpmapp.entities.AgendaPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AgendaPushSubscriptionRepository extends JpaRepository<AgendaPushSubscription, Long> {
    Optional<AgendaPushSubscription> findByEndpoint(String endpoint);

    Optional<AgendaPushSubscription> findByEndpointAndUser_Email(String endpoint, String email);

    @Query("SELECT subscription FROM AgendaPushSubscription subscription JOIN FETCH subscription.user")
    java.util.List<AgendaPushSubscription> findAllWithUser();
}
