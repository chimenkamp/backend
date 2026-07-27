package icpmapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "locations")
public class Location {

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

    private Integer capacity;

    public Location() {
    }

    public Location(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public Location(String id, String name, Integer capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }
}
