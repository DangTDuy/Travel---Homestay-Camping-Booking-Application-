package ut.edu.project.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    private String description;
    private int roomCount; // Số lượng phòng

    @ElementCollection
    private List<String> images; // Danh sách URL ảnh của homestay

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // Chủ homestay
}