package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pojo.Order;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAccountAccountId(Long accountId);
}
