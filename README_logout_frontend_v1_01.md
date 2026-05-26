# Todo App Spring Boot Security - Frontend Logout v1.01

## 1. Mục tiêu

Bản update `v1.01` bổ sung chức năng **Logout** cho frontend của Todo App.

Frontend cần:

- Hiển thị nút Logout ở trang Home/Todo.
- Gọi API logout của backend.
- Gửi `accessToken` trong `Authorization header`.
- Xóa token khỏi storage sau khi logout.
- Redirect user về trang Login.
- Chặn user vào Todo nếu không còn token.

Backend hiện tại đang dùng cơ chế:

```text
Logout -> lấy access token hiện tại -> lưu vào bảng invalid_tokens
```

Vì vậy sau khi logout, token cũ không nên dùng lại được.

---

## 2. API Logout

### Endpoint hiện tại

Do backend đang dùng:

```java
@RequestMapping("/auth")
```

nên endpoint hiện tại là:

```http
POST /auth/logout
```

Nếu backend đổi sang:

```java
@RequestMapping("/api/auth")
```

thì endpoint sẽ là:

```http
POST /api/auth/logout
```

Frontend cần xác nhận endpoint cuối cùng với backend trước khi tích hợp.

---

## 3. Request

### Header

```http
Authorization: Bearer <accessToken>
```

### Body

Không cần gửi body.

```json
{}
```

---

## 4. Response thành công

```json
{
  "code": 1000,
  "message": "Logout successful",
  "result": null
}
```

---

## 5. Flow logout frontend

```text
User đang ở Home/Todo
        |
        v
User bấm Logout
        |
        v
Frontend lấy accessToken từ localStorage/sessionStorage/memory
        |
        v
Frontend gọi POST /auth/logout
        |
        v
Backend lưu token vào invalid_tokens
        |
        v
Frontend xóa accessToken
        |
        v
Frontend redirect về Login
```

---

## 6. Code mẫu dùng Fetch

```javascript
async function logout() {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    window.location.href = "/login";
    return;
  }

  try {
    await fetch("http://localhost:8080/auth/logout", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
  } finally {
    localStorage.removeItem("accessToken");
    window.location.href = "/login";
  }
}
```

Nếu backend đổi endpoint thành `/api/auth/logout`, sửa URL thành:

```javascript
await fetch("http://localhost:8080/api/auth/logout", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`
  }
});
```

---

## 7. Code mẫu dùng Axios

```javascript
async function logout() {
  const token = localStorage.getItem("accessToken");

  try {
    if (token) {
      await axios.post(
        "http://localhost:8080/auth/logout",
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );
    }
  } finally {
    localStorage.removeItem("accessToken");
    window.location.href = "/login";
  }
}
```

---

## 8. Auth Guard đơn giản

```javascript
function requireAuth() {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    window.location.href = "/login";
    return false;
  }

  return true;
}
```

Dùng ở trang Todo/Home:

```javascript
if (!requireAuth()) {
  throw new Error("Unauthorized");
}
```

---

## 9. Xử lý lỗi 401

Nếu API Todo hoặc Logout trả `401 Unauthorized`, frontend nên:

```text
1. Xóa accessToken khỏi storage
2. Clear auth state nếu có
3. Redirect về Login
```

Ví dụ:

```javascript
function handleUnauthorized() {
  localStorage.removeItem("accessToken");
  window.location.href = "/login";
}
```

---

## 10. Task frontend

| Task | Tên việc | Mô tả | Kết quả cần đạt |
|---|---|---|---|
| FE-01 | Thêm nút Logout | Đặt nút Logout ở Home/Todo page | User thấy và bấm được Logout |
| FE-02 | Gọi API logout | Gọi `POST /auth/logout` kèm Bearer token | Backend nhận đúng token |
| FE-03 | Xóa token | Xóa `accessToken` khỏi storage | Token không còn lưu ở frontend |
| FE-04 | Redirect | Chuyển user về Login | User rời khỏi trang Todo |
| FE-05 | Auth Guard | Chặn vào Todo nếu không có token | Không login thì không vào Todo |
| FE-06 | Xử lý 401 | Nếu API trả 401 thì xóa token và về Login | Không bị treo UI |

---

## 11. Test case frontend

### TC-FE-01: Logout thành công

```text
Bước 1: Login thành công
Bước 2: Vào trang Todo
Bước 3: Bấm Logout
Kết quả: Gọi API logout, xóa token, redirect Login
```

### TC-FE-02: Không có token

```text
Bước 1: Xóa accessToken khỏi localStorage
Bước 2: Vào /todos
Kết quả: Frontend redirect về Login
```

### TC-FE-03: Token đã logout

```text
Bước 1: Login lấy token
Bước 2: Logout
Bước 3: Dùng lại token cũ gọi Todo API
Kết quả: Backend trả 401, frontend xóa token và về Login
```

### TC-FE-04: Backend tắt khi logout

```text
Bước 1: Tắt backend
Bước 2: Bấm Logout
Kết quả: Frontend vẫn xóa token local và redirect Login
```

---

## 12. Checklist bàn giao

- [ ] Biết endpoint chính xác: `/auth/logout` hoặc `/api/auth/logout`
- [ ] Logout request có `Authorization: Bearer <accessToken>`
- [ ] Không cần gửi body
- [ ] Response success có `code = 1000`
- [ ] Response success có `result = null`
- [ ] Sau logout luôn xóa `accessToken`
- [ ] Sau logout redirect về Login
- [ ] Protected route không cho vào nếu không có token
- [ ] API handler/interceptor xử lý `401 Unauthorized`
- [ ] Test lại token cũ sau logout không dùng được

---

## 13. Lưu ý cho frontend

Frontend **không cần biết chi tiết bảng `invalid_tokens`**, nhưng cần hiểu behavior:

```text
Sau logout, token cũ không nên dùng lại được.
```

Frontend vẫn phải xóa token local vì backend blacklist chỉ chặn request sau đó, không tự xóa token trong trình duyệt.

---

## 14. Version

```text
Document: README Frontend Logout
Project: Todo App Spring Boot Security
Update: v1.01
Status: Ready for frontend implementation
```
