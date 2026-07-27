package icpmapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @PrePersist
    protected void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @OneToMany(mappedBy = "track", fetch = FetchType.LAZY)
    private List<SessionHeader> sessions;

    public Track() {
    }

    public Track(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Track(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
