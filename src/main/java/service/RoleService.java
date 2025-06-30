package service;

import pojo.Role;
import java.util.List;

public interface RoleService {
    public Role getRole(String roleName);

    public Role insertRole(Role role);

    public Role getRoleById(Long id);

    public void deleteRole(Role role);

    public Role updateRole(Role role);

    public List<Role> getAllRoles();
}
