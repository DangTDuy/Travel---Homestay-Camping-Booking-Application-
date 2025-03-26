package ut.edu.project.repositories;

public class Camping {
    private String id;
    private String name;
    private String location;
    private double price;
    private String tentType;
    private String ownerId;

    public Camping() {
    }

    public Camping(String id, String name, String location, double price, String tentType, String ownerId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.tentType = tentType;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getTentType() {
        return tentType;
    }

    public void setTentType(String tentType) {
        this.tentType = tentType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Camping{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", price=" + price +
                ", tentType='" + tentType + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}
