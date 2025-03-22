package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "homestays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Homestay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
