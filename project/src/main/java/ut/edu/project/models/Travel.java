package ut.edu.project.models;

import jakarta.persistence.*;
import ut.edu.project.models.User;

@Entity
@Table(name = "travels")
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tourName;
    private String location;
    private double price;
    private String itinerary;

    @ManyToOne
    @JoinColumn(name = "guide_id", nullable = false)
    private User guide;

    // Constructors
    public Travel() {}

    public Travel(String tourName, String location, double price, String itinerary, User guide) {
        this.tourName = tourName;
        this.location = location;
        this.price = price;
        this.itinerary = itinerary;
        this.guide = guide;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public User getGuide() {
        return guide;
    }

    public void setGuide(User guide) {
        this.guide = guide;
    }
}
