package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pojo.Orchid;
import java.util.List;

public interface OrchidRepository extends JpaRepository<Orchid, Long> {
    public Orchid findByOrchidName(String orchidName);
    public List<Orchid> findByCategoryCategoryId(Long categoryId);
    public List<Orchid> findByOrchidNameContainingIgnoreCase(String name);
}
