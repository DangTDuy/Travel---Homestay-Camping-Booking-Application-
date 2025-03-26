package ut.edu.project.models;

import jakarta.persistence.*;

@Entity
@Table(name = "travels")
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tourName;
    private String location;
    private Double price;
    private String itinerary;

    @ManyToOne
    @JoinColumn(name = "guide_id", referencedColumnName = "id")
    private User guide;  // Giả sử đã có model User

    // Constructor, Getter, Setter
}
