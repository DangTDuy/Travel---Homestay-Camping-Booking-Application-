package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String description;

    @Column(nullable = false)
    private boolean active = true;
} 