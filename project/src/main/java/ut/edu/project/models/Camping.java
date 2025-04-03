package ut.edu.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @Positive(message = "Giá phải lớn hơn 0")
    private double price;

    @Min(value = 1, message = "Số chỗ phải ít nhất là 1")
    private int maxPlaces;

    private String description;

    private boolean isAvailable;

    private String image;

    @ElementCollection
    private List<String> bookings = new ArrayList<>();

    @ElementCollection
    private List<String> reviews = new ArrayList<>();

    @ElementCollection
    private List<String> additionalServices = new ArrayList<>();
}