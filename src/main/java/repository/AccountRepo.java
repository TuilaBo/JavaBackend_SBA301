package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pojo.Account;

public interface AccountRepo extends JpaRepository<Account, Long> {
    Account findByEmail(String email);
}
