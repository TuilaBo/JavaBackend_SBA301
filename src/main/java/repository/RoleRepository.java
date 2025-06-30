package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pojo.Role;

public interface RoleRepository  extends JpaRepository<Role,Long>{

    public Role findByRoleName(String name);

}
