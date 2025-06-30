package pojo;

import jakarta.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="orchid_id", nullable = false)
    private Orchid orchid;

    private double price;

    private int quantity;

    @OneToOne
    @JoinColumn(name= "order_id", nullable = false)
    private Order order;

    public OrderDetail() {
    }
    public OrderDetail(Long id, Orchid orchid, double price, int quantity, Order order) {
        this.id = id;
        this.orchid = orchid;
        this.price = price;
        this.quantity = quantity;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Orchid getOrchid() {
        return orchid;
    }

    public void setOrchid(Orchid orchid) {
        this.orchid = orchid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
