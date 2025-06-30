package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pojo.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    public Category findByCategoryName(String categoryName);
}
