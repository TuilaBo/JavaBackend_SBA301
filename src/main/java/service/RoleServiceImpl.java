package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Role;
import repository.RoleRepository;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role getRole(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public Role insertRole(Role role) {
        if (role != null && role.getRoleName() != null && !role.getRoleName().isEmpty()) {
           return roleRepository.save(role);
        } else {
            throw new IllegalArgumentException("Role or role name cannot be null or empty");
        }
    }
    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role updateRole(Role role) {
        if (role != null && role.getRoleId() != null) {
            return roleRepository.save(role);
        } else {
            throw new IllegalArgumentException("Role or role ID cannot be null");
        }
    }

    @Override
    public void deleteRole(Role role) {
        roleRepository.delete(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
