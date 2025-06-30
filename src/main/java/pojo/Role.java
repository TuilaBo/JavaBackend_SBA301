package pojo;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO,
            generator = "role_generator"
    )
        private Long roleId;

    @Column(length = 50, nullable = false, unique = true)
    private String roleName;

    public Role() {
    }
    public Role(Long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


}
