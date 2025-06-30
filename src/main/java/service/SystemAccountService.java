package service;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.Account;
import pojo.Role;
import repository.AccountRepo;

import java.util.Collections;

@Service
@Primary
public class SystemAccountService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SystemAccountService.class);

    private final AccountRepo accountRepo;
    private final PasswordEncoder passwordEncoder;

    public SystemAccountService(AccountRepo accountRepo, PasswordEncoder passwordEncoder) {
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Loading user by username: {}", email);
        Account account = accountRepo.findByEmail(email);
        if (account == null) {
            logger.warn("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        logger.info("Found user: {}, role: {}", account.getEmail(), account.getRole() != null ? account.getRole().getRoleName() : null);

        // Tạo UserDetails với password đã được mã hóa
        String roleName = account.getRole() != null ? account.getRole().getRoleName() : "USER";
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        
        logger.info("Creating authority: {} for role: {}", authority, roleName);
        
        UserDetails userDetails = new User(
                account.getEmail(),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(authority))
        );
        
        logger.info("Created UserDetails with authorities: {}", userDetails.getAuthorities());
        return userDetails;
    }

    public boolean validateUser(String email, String password) {
        logger.info("Validating user with email: {}", email);
        Account account = accountRepo.findByEmail(email);
        if (account == null) {
            logger.warn("No matching account found for email: {}", email);
            return false;
        }
        boolean matches = passwordEncoder.matches(password, account.getPassword());
        if (!matches) {
            logger.warn("Password does not match for email: {}", email);
        }
        return matches;
    }

    public Account findByEmail(String email) {
        return accountRepo.findByEmail(email);
    }

    // Method để tạo user mới với password được mã hóa
    public Account createUser(String email, String password, Role role, String accountName) {
        Account account = new Account();
        account.setEmail(email); // Chú ý: Email với E hoa
        account.setPassword(passwordEncoder.encode(password)); // Mã hóa password
        account.setRole(role); // Role là object, không phải String
        account.setAccountName(accountName); // accountName thay cho username
        return accountRepo.save(account);
    }

    // Method để cập nhật role của user
    public Account updateUserRole(Long userId, Role role) {
        Account account = accountRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        account.setRole(role);
        return accountRepo.save(account);
    }
}

