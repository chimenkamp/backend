package icpmapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "agenda_reminder_deliveries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "session_id"})
)
public class AgendaReminderDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private AgendaPushSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionHeader session;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;
}
