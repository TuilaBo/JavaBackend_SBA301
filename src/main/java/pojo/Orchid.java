package pojo;

import jakarta.persistence.*;

@Entity
@Table
public class Orchid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orchidId;

    private Boolean isNatural;

    private String orchidDescription;

    private String orchidName;

    private String orchidUrl;

    private double price;


    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Orchid() {
    }

    public Orchid(Long orchidId, Boolean isNatural, String orchidDescription, String orchidName, String orchidUrl, double price, Category category) {
        this.orchidId = orchidId;
        this.isNatural = isNatural;
        this.orchidDescription = orchidDescription;
        this.orchidName = orchidName;
        this.orchidUrl = orchidUrl;
        this.price = price;
        this.category = category;
    }

    public Long getOrchidId() {
        return orchidId;
    }

    public void setOrchidId(Long orchidId) {
        this.orchidId = orchidId;
    }

    public Boolean getNatural() {
        return isNatural;
    }

    public void setNatural(Boolean natural) {
        isNatural = natural;
    }

    public String getOrchidDescription() {
        return orchidDescription;
    }

    public void setOrchidDescription(String orchidDescription) {
        this.orchidDescription = orchidDescription;
    }

    public String getOrchidName() {
        return orchidName;
    }

    public void setOrchidName(String orchidName) {
        this.orchidName = orchidName;
    }

    public String getOrchidUrl() {
        return orchidUrl;
    }

    public void setOrchidUrl(String orchidUrl) {
        this.orchidUrl = orchidUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
