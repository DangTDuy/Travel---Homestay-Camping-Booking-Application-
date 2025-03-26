package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "campings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double price;
    private String description;
    private int tentCapacity; // Số lượng lều có thể chứa

    @ElementCollection
    private List<String> images; // Danh sách URL ảnh của khu cắm trại

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // Chủ khu cắm trại
}