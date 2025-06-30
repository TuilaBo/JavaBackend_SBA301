# Debug Guide - Lỗi 403 Forbidden

## Các bước debug lỗi 403:

### 1. Chạy script SQL để setup database
```sql
-- Chạy file setup_database.sql trong SQL Server Management Studio
-- Hoặc copy nội dung và chạy từng phần
```

### 2. Test credentials
**Admin user:**
- Email: `admin@example.com`
- Password: `admin123`

**User thường:**
- Email: `user@example.com` 
- Password: `user123`

### 3. Test API theo thứ tự:

#### Bước 1: Đăng nhập để lấy token
```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "email": "admin@example.com",
    "password": "admin123"
}
```

#### Bước 2: Test API không cần quyền
```bash
GET http://localhost:8080/auth/test-auth
Authorization: Bearer <token_from_step_1>
```

#### Bước 3: Test API cần quyền USER
```bash
GET http://localhost:8080/auth/test-user
Authorization: Bearer <token_from_step_1>
```

#### Bước 4: Test API cần quyền ADMIN
```bash
GET http://localhost:8080/auth/test-admin
Authorization: Bearer <token_from_step_1>
```

#### Bước 5: Kiểm tra thông tin user
```bash
GET http://localhost:8080/auth/me
Authorization: Bearer <token_from_step_1>
```

### 4. Kiểm tra logs

Chạy ứng dụng với logging chi tiết:
```bash
mvn spring-boot:run
```

Tìm các log sau:
- `JwtFilter` - Xem JWT có được xử lý đúng không
- `SystemAccountService` - Xem authorities có được tạo đúng không
- `WebSecurityConfig` - Xem security config

### 5. Các nguyên nhân có thể gây lỗi 403:

#### A. JWT Token không hợp lệ
- Token hết hạn
- Token không đúng format
- Token không chứa đúng thông tin user

#### B. Authorities không đúng
- Role trong database không có prefix `ROLE_`
- User không có role được assign
- Role name không khớp với `@PreAuthorize("hasRole('ADMIN')")`

#### C. Security Configuration
- URL pattern không match
- Method security không được enable
- Filter chain không đúng thứ tự

### 6. Kiểm tra database

```sql
-- Kiểm tra roles
SELECT * FROM roles;

-- Kiểm tra accounts và roles
SELECT a.account_id, a.email, a.account_name, a.is_active, r.role_name 
FROM accounts a 
LEFT JOIN roles r ON a.role_id = r.role_id;

-- Kiểm tra admin user
SELECT a.*, r.role_name 
FROM accounts a 
LEFT JOIN roles r ON a.role_id = r.role_id 
WHERE a.email = 'admin@example.com';
```

### 7. Expected Authorities

Khi đăng nhập thành công với admin user, authorities phải là:
```
[ROLE_ADMIN]
```

Khi đăng nhập với user thường:
```
[ROLE_USER]
```

### 8. Debug với Postman/curl

```bash
# Test login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'

# Test admin access
curl -X GET http://localhost:8080/auth/test-admin \
  -H "Authorization: Bearer <token>"

# Test user info
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer <token>"
```

### 9. Common Fixes

#### Fix 1: Kiểm tra role trong database
```sql
-- Đảm bảo role name là 'ADMIN' (không phải 'ROLE_ADMIN')
UPDATE roles SET role_name = 'ADMIN' WHERE role_name = 'ROLE_ADMIN';
```

#### Fix 2: Kiểm tra user có role đúng
```sql
-- Assign admin role cho user
UPDATE accounts 
SET role_id = (SELECT role_id FROM roles WHERE role_name = 'ADMIN')
WHERE email = 'admin@example.com';
```

#### Fix 3: Kiểm tra account active
```sql
-- Đảm bảo account không bị disable
UPDATE accounts SET is_active = 1 WHERE email = 'admin@example.com';
```

### 10. Test với Swagger UI

1. Mở `http://localhost:8080/swagger-ui.html`
2. Click "Authorize" button
3. Nhập token: `Bearer <your_jwt_token>`
4. Test các API

### 11. Log Analysis

Tìm các log pattern sau:

**Success pattern:**
```
JwtFilter: JWT token is valid for email: admin@example.com
SystemAccountService: Creating authority: ROLE_ADMIN for role: ADMIN
SystemAccountService: Created UserDetails with authorities: [ROLE_ADMIN]
JwtFilter: Authentication set in SecurityContext for user: admin@example.com
```

**Error pattern:**
```
JwtFilter: JWT token validation failed for email: admin@example.com
SystemAccountService: User not found with email: admin@example.com
```

### 12. Final Check

Nếu vẫn gặp lỗi, hãy kiểm tra:

1. Database connection
2. JWT secret key
3. Spring Security configuration
4. Method security annotations
5. Token expiration time 