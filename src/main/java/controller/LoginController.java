package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pojo.Account;
import pojo.Role;
import security.JwtUtil;
import service.RoleService;
import service.SystemAccountService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and role management")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SystemAccountService systemAccountService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RoleService roleService;

    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Check if user exists and is active
            Account account = systemAccountService.findByEmail(loginRequest.getEmail());
            if (account == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                errorResponse.put("message", "Email is not registered");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            if (!account.isActive()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account disabled");
                errorResponse.put("message", "Your account has been deactivated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", userDetails.getUsername());
            response.put("accountName", account.getAccountName());
            response.put("accountId", account.getAccountId());
            response.put("role", account.getRole() != null ? account.getRole().getRoleName() : "USER");
            response.put("isActive", account.isActive());
            response.put("authorities", userDetails.getAuthorities());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "User registration", description = "Registers a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate input
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid input");
                errorResponse.put("message", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid input");
                errorResponse.put("message", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (registerRequest.getAccountName() == null || registerRequest.getAccountName().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid input");
                errorResponse.put("message", "Account name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Check if user already exists
            Account existingAccount = systemAccountService.findByEmail(registerRequest.getEmail());
            if (existingAccount != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User already exists");
                errorResponse.put("message", "Email is already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Get default USER role
            Role userRole = roleService.getRole("USER");
            if (userRole == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "System error");
                errorResponse.put("message", "Default user role not found");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Create new account
            Account createdAccount = systemAccountService.createUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    userRole,
                    registerRequest.getAccountName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("accountId", createdAccount.getAccountId());
            response.put("email", createdAccount.getEmail());
            response.put("accountName", createdAccount.getAccountName());
            response.put("role", createdAccount.getRole().getRoleName());
            response.put("isActive", createdAccount.isActive());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Create new role", description = "Creates a new role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Role already exists")
    })
    @PostMapping("/roles")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody CreateRoleRequest createRoleRequest) {
        try {
            // Validate input
            if (createRoleRequest.getRoleName() == null || createRoleRequest.getRoleName().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid input");
                errorResponse.put("message", "Role name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Check if role already exists
            Role existingRole = roleService.getRole(createRoleRequest.getRoleName());
            if (existingRole != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Role already exists");
                errorResponse.put("message", "Role with name '" + createRoleRequest.getRoleName() + "' already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Create new role
            Role newRole = new Role();
            newRole.setRoleName(createRoleRequest.getRoleName());
            Role createdRole = roleService.insertRole(newRole);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Role created successfully");
            response.put("roleId", createdRole.getRoleId());
            response.put("roleName", createdRole.getRoleName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Role creation failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Get all roles", description = "Returns list of all roles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved roles")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Update user role", description = "Updates user's role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable Long userId, @RequestBody UpdateUserRoleRequest request) {
        try {
            // Validate input
            if (request.getRoleId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid input");
                errorResponse.put("message", "Role ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Get role
            Role role = roleService.getRoleById(request.getRoleId());
            if (role == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Role not found");
                errorResponse.put("message", "Role with ID " + request.getRoleId() + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Update user role
            Account updatedAccount = systemAccountService.updateUserRole(userId, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            response.put("accountId", updatedAccount.getAccountId());
            response.put("email", updatedAccount.getEmail());
            response.put("accountName", updatedAccount.getAccountName());
            response.put("role", updatedAccount.getRole().getRoleName());
            response.put("isActive", updatedAccount.isActive());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Update failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Validate token", description = "Validates the JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is invalid")
    })
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.extractEmail(token);
                
                if (email != null && jwtUtil.validateToken(token, email)) {
                    Account account = systemAccountService.findByEmail(email);
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("email", email);
                    response.put("accountId", account.getAccountId());
                    response.put("accountName", account.getAccountName());
                    response.put("role", account.getRole() != null ? account.getRole().getRoleName() : "USER");
                    response.put("isActive", account.isActive());
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Token validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "Get current user info", description = "Returns current user information and authorities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Account account = systemAccountService.findByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("accountId", account.getAccountId());
            response.put("email", account.getEmail());
            response.put("accountName", account.getAccountName());
            response.put("role", account.getRole() != null ? account.getRole().getRoleName() : "USER");
            response.put("isActive", account.isActive());
            response.put("authorities", authentication.getAuthorities());
            response.put("principal", authentication.getPrincipal());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get user info");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "Test admin access", description = "Test endpoint for admin access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin access granted"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/test-admin")
    public ResponseEntity<Map<String, Object>> testAdminAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Test user access", description = "Test endpoint for user access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User access granted"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/test-user")
    public ResponseEntity<Map<String, Object>> testUserAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User access granted");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Test authenticated access", description = "Test endpoint for any authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuthenticatedAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authenticated access granted");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // Inner classes for request/response
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String accountName;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }
    }

    public static class CreateRoleRequest {
        private String roleName;

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }

    public static class UpdateUserRoleRequest {
        private Long roleId;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }
    }
} 