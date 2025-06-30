# Orchid Management System - Spring Boot API

Hệ thống quản lý hoa lan được xây dựng bằng Spring Boot với JWT authentication.

## Tính năng chính

- **Authentication & Authorization**: Đăng ký, đăng nhập với JWT token
- **Quản lý hoa lan**: CRUD operations cho hoa lan
- **Quản lý danh mục**: CRUD operations cho danh mục hoa lan
- **Quản lý đơn hàng**: Tạo và quản lý đơn hàng
- **Quản lý vai trò**: Hệ thống phân quyền với ADMIN và USER
- **Swagger UI**: API documentation tự động

## Cấu trúc dự án

```
src/main/java/
├── com/se170395/orchid/
│   └── OrchidApplication.java
├── config/
│   ├── SwaggerConfig.java
│   ├── WebConfig.java
│   └── WebSecurityConfig.java
├── controller/
│   ├── AccountController.java
│   ├── CategoryController.java
│   ├── LoginController.java
│   ├── OrchidController.java
│   └── OrderController.java
├── pojo/
│   ├── Account.java
│   ├── Category.java
│   ├── Orchid.java
│   ├── Order.java
│   ├── OrderDetail.java
│   └── Role.java
├── repository/
│   ├── AccountRepo.java
│   ├── CategoryRepository.java
│   ├── OrchidRepository.java
│   └── OrderRepository.java
├── security/
│   ├── JwtFilter.java
│   └── JwtUtil.java
└── service/
    ├── CategoryService.java
    ├── OrchidService.java
    ├── OrderService.java
    ├── RoleService.java
    └── SystemAccountService.java
```

## Cài đặt và chạy

### Yêu cầu hệ thống
- Java 17
- Maven 3.6+
- SQL Server 2019+

### Cài đặt database
1. Tạo database tên `Orchid` trong SQL Server
2. Chạy script `setup_database.sql` để tạo bảng và dữ liệu mẫu

### Cấu hình
1. Cập nhật thông tin database trong `application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=Orchid;encrypt=true;trustServerCertificate=true
spring.datasource.username=SA
spring.datasource.password=12345
```

### Chạy ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: http://localhost:8080

## API Documentation

Truy cập Swagger UI: http://localhost:8080/swagger-ui.html

## Các API chính

### Authentication (`/auth`)
- `POST /auth/register` - Đăng ký tài khoản mới
- `POST /auth/login` - Đăng nhập
- `POST /auth/validate` - Xác thực JWT token
- `GET /auth/me` - Lấy thông tin user hiện tại
- `GET /auth/roles` - Lấy danh sách roles
- `POST /auth/roles` - Tạo role mới
- `PUT /auth/users/{userId}/role` - Cập nhật role cho user

### Orchid Management (`/api/orchids`)
- `GET /api/orchids` - Lấy danh sách tất cả hoa lan
- `GET /api/orchids/{id}` - Lấy hoa lan theo ID
- `POST /api/orchids` - Tạo hoa lan mới
- `PUT /api/orchids/{id}` - Cập nhật hoa lan
- `DELETE /api/orchids/{id}` - Xóa hoa lan
- `GET /api/orchids/category/{categoryId}` - Lấy hoa lan theo danh mục
- `GET /api/orchids/search?name={name}` - Tìm kiếm hoa lan theo tên

### Category Management (`/api/categories`)
- `GET /api/categories` - Lấy danh sách tất cả danh mục
- `GET /api/categories/{id}` - Lấy danh mục theo ID
- `POST /api/categories` - Tạo danh mục mới
- `PUT /api/categories/{id}` - Cập nhật danh mục
- `DELETE /api/categories/{id}` - Xóa danh mục
- `GET /api/categories/name/{name}` - Lấy danh mục theo tên

### Order Management (`/api/orders`)
- `GET /api/orders` - Lấy danh sách tất cả đơn hàng
- `GET /api/orders/{id}` - Lấy đơn hàng theo ID
- `POST /api/orders` - Tạo đơn hàng mới
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng
- `GET /api/orders/my-orders` - Lấy đơn hàng của user hiện tại
- `POST /api/orders/{orderId}/items` - Thêm sản phẩm vào đơn hàng

## Phân quyền

**Lưu ý quan trọng**: Hệ thống đã được cấu hình để **không có giới hạn phân quyền** ở phía backend. Việc phân quyền sẽ được xử lý hoàn toàn ở phía frontend.

### Cách xử lý phân quyền ở Frontend

1. **Lấy thông tin user và role**:
   ```javascript
   // Sau khi đăng nhập thành công
   const userInfo = response.data;
   const userRole = userInfo.role; // "ADMIN" hoặc "USER"
   ```

2. **Kiểm tra quyền trước khi gọi API**:
   ```javascript
   // Ví dụ: Chỉ admin mới được tạo/sửa/xóa hoa lan
   if (userRole === 'ADMIN') {
     // Hiển thị nút Create, Edit, Delete
     // Cho phép gọi API POST, PUT, DELETE
   } else {
     // Chỉ hiển thị nút View
     // Chỉ cho phép gọi API GET
   }
   ```

3. **Ẩn/hiện UI elements**:
   ```javascript
   // Ẩn các chức năng admin nếu user không phải admin
   const isAdmin = userRole === 'ADMIN';
   document.getElementById('createOrchidBtn').style.display = isAdmin ? 'block' : 'none';
   document.getElementById('editOrchidBtn').style.display = isAdmin ? 'block' : 'none';
   document.getElementById('deleteOrchidBtn').style.display = isAdmin ? 'block' : 'none';
   ```

4. **Bảo vệ routes**:
   ```javascript
   // React Router example
   const ProtectedRoute = ({ children, requiredRole }) => {
     const userRole = getUserRole(); // Lấy từ localStorage hoặc context
     
     if (userRole !== requiredRole) {
       return <Navigate to="/unauthorized" />;
     }
     
     return children;
   };
   
   // Sử dụng
   <ProtectedRoute requiredRole="ADMIN">
     <AdminDashboard />
   </ProtectedRoute>
   ```

## Dữ liệu mẫu

Sau khi chạy `setup_database.sql`, hệ thống sẽ có:

### Roles
- ADMIN: Quản trị viên
- USER: Người dùng thường

### Accounts
- Admin: admin@orchid.com / 12345
- User: user@orchid.com / 12345

### Categories
- Hoa lan rừng
- Hoa lan công nghiệp
- Hoa lan lai tạo

### Orchids
- Nhiều loại hoa lan mẫu với thông tin đầy đủ

## Troubleshooting

### Lỗi 403 Forbidden
Nếu gặp lỗi 403, kiểm tra:
1. JWT token có hợp lệ không
2. Token có được gửi đúng format không: `Bearer <token>`
3. User có tồn tại và active không

### Lỗi Database Connection
1. Kiểm tra SQL Server có đang chạy không
2. Kiểm tra thông tin connection string
3. Kiểm tra database `Orchid` đã được tạo chưa

### Lỗi Swagger UI
1. Truy cập: http://localhost:8080/swagger-ui.html
2. Kiểm tra console browser có lỗi gì không
3. Đảm bảo application đã start thành công

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.2.0, Spring Security, JWT
- **Database**: SQL Server
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Java Version**: 17

## Đóng góp

1. Fork dự án
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

MIT License 