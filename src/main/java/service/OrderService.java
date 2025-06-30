package service;

import pojo.Order;
import pojo.OrderDetail;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order createOrder(Order order);
    Order updateOrderStatus(Long id, String status);
    List<Order> getOrdersByUser();
    OrderDetail addOrderItem(Long orderId, OrderDetail orderDetail);
    boolean isOrderOwner(Long orderId, String username);
} 