package controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.Role;
import service.RoleService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * Endpoint to insert a new role.
     */

    @Operation(summary = "Create a new role", description = "Creates a new role in the system")
    @ApiResponse(responseCode = "201", description = "Role created successfully",
            content = @Content(schema = @Schema(implementation = Role.class)))
    @PostMapping
    public ResponseEntity<Role> insertRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleService.insertRole(role));
    }

    @Operation(summary = "Get role by ID", description = "Retrieves a role by its ID")
    @ApiResponse(responseCode = "200", description = "Role found",
            content = @Content(schema = @Schema(implementation = Role.class)))
    @ApiResponse(responseCode = "404", description = "Role not found")
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @Operation(summary = "Update an existing role", description = "Updates the details of an existing role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(roleService.insertRole(role));
    }

    @Operation(summary = "Delete a role", description = "Deletes a role from the system")
    @ApiResponse(responseCode = "204", description = "Role deleted successfully")
    @ApiResponse(responseCode = "404", description = "Role not found")
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role != null) {
            roleService.deleteRole(role);
        } else {
            throw new IllegalArgumentException("Role with ID " + id + " does not exist");
        }
    }


}
