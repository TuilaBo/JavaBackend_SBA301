package pojo;


import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accountId;

    @Column(length = 50, nullable = false, unique = true)
    private String accountName;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;


    public Account() {
    }

    public Account(Long accountId, String accountName, String email, String password, Role role) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }

    public Account(Long accountId, String accountName, String email, String password, boolean isActive, Role role) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.role = role;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
