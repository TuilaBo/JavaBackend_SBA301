# Hướng dẫn xử lý phân quyền ở Frontend

## Tổng quan

Backend đã được cấu hình để **không có giới hạn phân quyền**. Tất cả API endpoints đều có thể truy cập mà không cần kiểm tra role. Việc phân quyền sẽ được xử lý hoàn toàn ở phía frontend.

## Cách lấy thông tin user và role

### 1. Sau khi đăng nhập thành công

```javascript
// API Response từ /auth/login
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@orchid.com",
  "accountName": "Admin User",
  "accountId": 1,
  "role": "ADMIN",  // ← Đây là role của user
  "isActive": true,
  "authorities": ["ROLE_ADMIN"],
  "message": "Login successful"
}
```

### 2. Lưu trữ thông tin user

```javascript
// Lưu vào localStorage
localStorage.setItem('userToken', response.data.token);
localStorage.setItem('userRole', response.data.role);
localStorage.setItem('userInfo', JSON.stringify(response.data));

// Hoặc lưu vào state management (Redux, Context, etc.)
```

## Kiểm tra quyền trước khi gọi API

### 1. Kiểm tra role trước khi hiển thị UI

```javascript
const userRole = localStorage.getItem('userRole');

// Chỉ admin mới được tạo/sửa/xóa hoa lan
if (userRole === 'ADMIN') {
  // Hiển thị nút Create, Edit, Delete
  showAdminButtons();
} else {
  // Chỉ hiển thị nút View
  hideAdminButtons();
}
```

### 2. Ẩn/hiện UI elements

```javascript
function updateUIByRole() {
  const isAdmin = localStorage.getItem('userRole') === 'ADMIN';
  
  // Ẩn/hiện các nút admin
  document.getElementById('createOrchidBtn').style.display = isAdmin ? 'block' : 'none';
  document.getElementById('editOrchidBtn').style.display = isAdmin ? 'block' : 'none';
  document.getElementById('deleteOrchidBtn').style.display = isAdmin ? 'block' : 'none';
  
  // Ẩn/hiện các menu admin
  document.getElementById('adminMenu').style.display = isAdmin ? 'block' : 'none';
  
  // Thay đổi text hoặc class
  if (isAdmin) {
    document.getElementById('userInfo').textContent = 'Admin Dashboard';
  } else {
    document.getElementById('userInfo').textContent = 'User Dashboard';
  }
}
```

## Bảo vệ Routes (React Router)

### 1. Tạo ProtectedRoute component

```javascript
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, requiredRole }) => {
  const userRole = localStorage.getItem('userRole');
  
  if (!userRole) {
    return <Navigate to="/login" />;
  }
  
  if (requiredRole && userRole !== requiredRole) {
    return <Navigate to="/unauthorized" />;
  }
  
  return children;
};
```

### 2. Sử dụng trong App.js

```javascript
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Routes cho tất cả user đã đăng nhập */}
        <Route path="/" element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />
        
        {/* Routes chỉ cho admin */}
        <Route path="/admin" element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminDashboard />
          </ProtectedRoute>
        } />
        
        <Route path="/manage-orchids" element={
          <ProtectedRoute requiredRole="ADMIN">
            <OrchidManagement />
          </ProtectedRoute>
        } />
        
        <Route path="/manage-categories" element={
          <ProtectedRoute requiredRole="ADMIN">
            <CategoryManagement />
          </ProtectedRoute>
        } />
        
        <Route path="/unauthorized" element={<Unauthorized />} />
      </Routes>
    </BrowserRouter>
  );
}
```

## Xử lý API calls

### 1. Tạo API service với kiểm tra quyền

```javascript
class ApiService {
  constructor() {
    this.baseURL = 'http://localhost:8080';
    this.token = localStorage.getItem('userToken');
  }
  
  // Kiểm tra quyền trước khi gọi API
  async callApi(endpoint, method = 'GET', data = null, requiredRole = null) {
    const userRole = localStorage.getItem('userRole');
    
    // Kiểm tra quyền nếu có yêu cầu
    if (requiredRole && userRole !== requiredRole) {
      throw new Error('Không có quyền truy cập');
    }
    
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`
    };
    
    const config = {
      method,
      headers,
      body: data ? JSON.stringify(data) : null
    };
    
    const response = await fetch(`${this.baseURL}${endpoint}`, config);
    return response.json();
  }
  
  // API cho tất cả user
  async getAllOrchids() {
    return this.callApi('/api/orchids');
  }
  
  async getOrchidById(id) {
    return this.callApi(`/api/orchids/${id}`);
  }
  
  // API chỉ cho admin
  async createOrchid(orchidData) {
    return this.callApi('/api/orchids', 'POST', orchidData, 'ADMIN');
  }
  
  async updateOrchid(id, orchidData) {
    return this.callApi(`/api/orchids/${id}`, 'PUT', orchidData, 'ADMIN');
  }
  
  async deleteOrchid(id) {
    return this.callApi(`/api/orchids/${id}`, 'DELETE', null, 'ADMIN');
  }
}
```

### 2. Sử dụng trong components

```javascript
function OrchidList() {
  const [orchids, setOrchids] = useState([]);
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole'));
  
  useEffect(() => {
    loadOrchids();
  }, []);
  
  const loadOrchids = async () => {
    try {
      const data = await apiService.getAllOrchids();
      setOrchids(data);
    } catch (error) {
      console.error('Lỗi khi tải danh sách hoa lan:', error);
    }
  };
  
  const handleDelete = async (id) => {
    if (userRole !== 'ADMIN') {
      alert('Bạn không có quyền xóa hoa lan');
      return;
    }
    
    try {
      await apiService.deleteOrchid(id);
      loadOrchids(); // Reload danh sách
    } catch (error) {
      console.error('Lỗi khi xóa hoa lan:', error);
    }
  };
  
  return (
    <div>
      <h2>Danh sách hoa lan</h2>
      
      {userRole === 'ADMIN' && (
        <button onClick={() => navigate('/orchids/create')}>
          Thêm hoa lan mới
        </button>
      )}
      
      {orchids.map(orchid => (
        <div key={orchid.orchidId}>
          <h3>{orchid.orchidName}</h3>
          <p>{orchid.description}</p>
          <p>Giá: {orchid.price}</p>
          
          {userRole === 'ADMIN' && (
            <div>
              <button onClick={() => navigate(`/orchids/edit/${orchid.orchidId}`)}>
                Sửa
              </button>
              <button onClick={() => handleDelete(orchid.orchidId)}>
                Xóa
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
```

## Xử lý lỗi và thông báo

### 1. Tạo component thông báo

```javascript
function showNotification(message, type = 'info') {
  // Sử dụng toast library hoặc custom notification
  if (type === 'error') {
    alert(`Lỗi: ${message}`);
  } else {
    alert(message);
  }
}

// Sử dụng
try {
  await apiService.createOrchid(data);
  showNotification('Tạo hoa lan thành công', 'success');
} catch (error) {
  if (error.message === 'Không có quyền truy cập') {
    showNotification('Bạn không có quyền thực hiện hành động này', 'error');
  } else {
    showNotification('Có lỗi xảy ra', 'error');
  }
}
```

### 2. Redirect khi không có quyền

```javascript
function handleUnauthorized() {
  showNotification('Bạn không có quyền truy cập trang này', 'error');
  navigate('/');
}

// Sử dụng trong ProtectedRoute
if (requiredRole && userRole !== requiredRole) {
  handleUnauthorized();
  return null;
}
```

## Best Practices

1. **Luôn kiểm tra quyền ở frontend** trước khi gọi API
2. **Lưu trữ thông tin user** một cách an toàn (localStorage, sessionStorage, hoặc state management)
3. **Xử lý lỗi** một cách graceful khi user không có quyền
4. **Cập nhật UI** ngay lập tức khi role thay đổi
5. **Logout** khi token hết hạn hoặc có lỗi authentication
6. **Validate input** ở frontend trước khi gửi lên server

## Ví dụ hoàn chỉnh

Xem file `example-frontend.js` để có ví dụ hoàn chỉnh về cách implement phân quyền ở frontend. 