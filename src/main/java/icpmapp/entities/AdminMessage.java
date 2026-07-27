package icpmapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "admin_messages")
public class AdminMessage {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessagePriority priority = MessagePriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "admin_message_recipients",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> recipients;

    @Column(name = "recipient_count")
    private Integer recipientCount = 0;

    @Column(name = "read_count")
    private Integer readCount = 0;

    @Column(name = "push_sent")
    private Boolean pushSent = false;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "sent_by")
    private String sentBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "admin_message_read_by",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> readBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        sentAt = LocalDateTime.now();
    }

    public AdminMessage() {
    }

    public AdminMessage(String title, String message, MessagePriority priority, RecipientType recipientType,
                        Boolean pushSent, Boolean emailSent, String sentBy) {
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.recipientType = recipientType;
        this.pushSent = pushSent;
        this.emailSent = emailSent;
        this.sentBy = sentBy;
    }
}
