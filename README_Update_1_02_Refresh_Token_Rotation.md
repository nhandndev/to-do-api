# Todo App Spring Boot Security - Update 1.02

## Access Token & Refresh Token Rotation

Tài liệu README này mô tả bản update **1.02** của dự án **Todo App - Spring Boot Security**.  
Mục tiêu của update này là bổ sung cơ chế **Access Token + Refresh Token Rotation** cho cả backend và frontend.

---

## 1. Mục tiêu update 1.02

Bản 1.02 tập trung vào phần authentication token lifecycle.

Các mục tiêu chính:

```txt
Login trả về accessToken và refreshToken
Protected API dùng Bearer accessToken
Refresh token được lưu trong database
Khi refresh thành công thì rotate refresh token
Logout sẽ revoke refresh token
Frontend tự refresh token khi access token hết hạn
```

---

## 2. Phạm vi update

### 2.1 In Scope

Update này bao gồm:

```txt
Backend Spring Boot Security/JWT
RefreshToken entity/repository/service
API login trả 2 token
API refresh-token có rotation
API logout revoke refresh token
JwtAuthenticationFilter chỉ xác thực access token
Frontend lưu accessToken và refreshToken
Frontend gửi Authorization: Bearer accessToken
Frontend tự refresh token khi access token hết hạn
Frontend retry request cũ sau khi refresh thành công
```

### 2.2 Out of Scope

Update này chưa làm:

```txt
Không hash refresh token
Không dùng HttpOnly Cookie
Không blacklist access token
Không làm OAuth
Không làm forgot password
Không làm admin role
Không làm device/session management
```

> Ghi chú: Refresh token được lưu dạng token string trong database để MVP dễ triển khai.

---

## 3. Quy trình làm dự án

Dự án nên đi theo flow:

```txt
Requirement
→ User Story
→ Feature
→ Use Case / Flow
→ Database Design
→ API Design
→ DTO / Entity / Repository / Service
→ Code
→ Test
```

Không nên bắt đầu ngay bằng controller hoặc entity nếu chưa rõ flow nghiệp vụ.

---

## 4. User Stories

| Mã | User Story | Feature |
|---|---|---|
| US-10 | User muốn đăng nhập để lấy quyền truy cập API cá nhân | Login trả accessToken + refreshToken |
| US-11 | Frontend muốn gọi Todo API bằng token hợp lệ | Bearer Access Token |
| US-12 | User muốn tiếp tục phiên làm việc khi access token hết hạn | Refresh Access Token |
| US-13 | Backend muốn refresh token cũ không dùng lại sau khi refresh | Rotate Refresh Token |
| US-14 | User muốn logout để refresh token không dùng lại được | Logout revoke refresh token |
| US-15 | Frontend muốn tự refresh token và retry request | Frontend API Interceptor |

---

## 5. Feature Backlog

| Mã | Feature | Priority | Kết quả cần đạt |
|---|---|---|---|
| F12 | Login trả accessToken + refreshToken | Must Have | Login đúng trả đủ 2 token |
| F13 | RefreshToken DB | Must Have | Có bảng refresh_tokens lưu refresh token |
| F14 | Refresh Access Token | Must Have | Refresh token hợp lệ trả access token mới |
| F15 | Rotate Refresh Token | Must Have | Refresh token cũ bị revoke, token mới được tạo |
| F16 | Logout revoke refresh token | Must Have | Logout xong refresh token không dùng lại được |
| F17 | Frontend Bearer Access Token | Must Have | Frontend gửi Bearer access token |
| F18 | Frontend Auto Refresh & Retry | Should Have | Token hết hạn thì tự refresh và gọi lại API |
| F19 | Token Error Handling | Should Have | Refresh fail thì clear token và về login |

---

## 6. Database Design

Bản update 1.02 giữ nguyên các bảng hiện có:

```txt
users
todos
```

và bổ sung bảng:

```txt
refresh_tokens
```

Quan hệ:

```txt
users.id 1 -------- n refresh_tokens.user_id
```

---

## 7. Bảng users

| Field | Kiểu | Bắt buộc | Ghi chú |
|---|---|---|---|
| id | Long | Có | Primary key |
| username | String | Có | Unique |
| email | String | Có | Unique |
| password | String | Có | BCrypt hash |
| role | Enum/String | Có | Mặc định USER |
| enabled | Boolean | Có | Mặc định true |
| createdAt | LocalDateTime | Có | Ngày tạo |
| updatedAt | LocalDateTime | Có | Ngày cập nhật |

---

## 8. Bảng refresh_tokens

| Field | Kiểu | Bắt buộc | Ghi chú |
|---|---|---|---|
| id | Long | Có | Primary key |
| token | String/Text | Có | Lưu refresh token dạng token string |
| revoked | Boolean | Có | Mặc định false |
| expiredAt | LocalDateTime | Có | Thời điểm hết hạn |
| user_id | Long | Có | Foreign key tới users.id |
| createdAt | LocalDateTime | Có | Ngày tạo |
| updatedAt | LocalDateTime | Có | Ngày cập nhật |

---

## 9. Entity Relationship

### User entity

```java
@OneToMany(mappedBy = "user")
private List<RefreshToken> refreshTokens;
```

### RefreshToken entity

```java
@ManyToOne
@JoinColumn(name = "user_id")
private User user;
```

Lý do cần bảng `refresh_tokens`:

```txt
Backend cần biết refresh token còn hợp lệ không
Backend cần biết refresh token đã logout/revoke chưa
Backend cần revoke refresh token cũ khi rotate
Backend cần chặn dùng lại refresh token cũ
```

---

## 10. Security Design

### 10.1 Access Token

Access token dùng để gọi API protected.

```txt
Thời gian sống ngắn
Không lưu DB
Gửi qua header Authorization
Format: Bearer <accessToken>
```

Ví dụ:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Protected APIs:

```txt
/api/users/me
/api/todos/**
```

---

### 10.2 Refresh Token

Refresh token dùng để xin token mới.

```txt
Thời gian sống dài hơn access token
Có lưu trong database
Không gửi làm Bearer token cho Todo API
Chỉ gửi trong body của /api/auth/refresh-token và /api/auth/logout
```

---

### 10.3 Refresh Token Rotation

Refresh token rotation nghĩa là:

```txt
Mỗi lần dùng refresh token để lấy token mới
→ refresh token cũ bị revoked
→ backend tạo access token mới
→ backend tạo refresh token mới
→ frontend lưu lại cả 2 token mới
```

Ví dụ:

```txt
Ban đầu:
accessToken = A1
refreshToken = R1

Khi A1 hết hạn:
Frontend gửi R1 tới /api/auth/refresh-token

Backend:
revoke R1
tạo A2
tạo R2
trả A2 + R2

Sau đó:
R1 không dùng lại được
Frontend dùng A2 + R2
```

---

## 11. Authentication Flows

### 11.1 Login Flow

```txt
Client gọi POST /api/auth/login
→ Backend kiểm tra username/email và password
→ Nếu đúng:
   → tạo accessToken
   → tạo refreshToken
   → lưu refreshToken vào DB
   → trả accessToken + refreshToken
→ Frontend lưu token
```

Response:

```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "authenticated": true
  }
}
```

---

### 11.2 Call Protected API Flow

```txt
Frontend lấy accessToken từ localStorage/state
→ Gắn vào header Authorization
→ Gọi API protected
→ Backend JwtAuthenticationFilter kiểm tra accessToken
→ Nếu hợp lệ, set SecurityContext
→ API xử lý request
```

Header:

```http
Authorization: Bearer <accessToken>
```

---

### 11.3 Refresh Token Flow

```txt
Frontend gọi API protected
→ Access token hết hạn
→ Backend trả 401
→ Frontend gọi POST /api/auth/refresh-token
→ Gửi refreshToken hiện tại trong body
→ Backend kiểm tra refresh token
→ Backend revoke refresh token cũ
→ Backend tạo accessToken mới và refreshToken mới
→ Frontend lưu token mới
→ Frontend gọi lại request cũ
```

Request:

```json
{
  "refreshToken": "..."
}
```

Response:

```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "authenticated": true
  }
}
```

---

### 11.4 Logout Flow

```txt
User bấm logout
→ Frontend gọi POST /api/auth/logout
→ Gửi refreshToken hiện tại
→ Backend tìm refreshToken trong DB
→ Backend set revoked = true
→ Frontend xóa accessToken và refreshToken
→ Chuyển về login
```

Request:

```json
{
  "refreshToken": "..."
}
```

Response:

```json
{
  "code": 1000,
  "message": "Logout successfully"
}
```

---

## 12. API Design

| Method | Endpoint | Quyền | Chức năng |
|---|---|---|---|
| POST | `/api/auth/register` | PUBLIC | Đăng ký |
| POST | `/api/auth/login` | PUBLIC | Đăng nhập |
| POST | `/api/auth/refresh-token` | PUBLIC | Refresh token rotation |
| POST | `/api/auth/logout` | PUBLIC hoặc USER | Logout revoke refresh token |
| GET | `/api/users/me` | USER | Lấy thông tin user hiện tại |
| GET | `/api/todos` | USER | Lấy danh sách todo |
| POST | `/api/todos` | USER | Tạo todo |
| GET | `/api/todos/{id}` | USER | Xem chi tiết todo |
| PUT | `/api/todos/{id}` | USER | Cập nhật todo |
| PATCH | `/api/todos/{id}/toggle` | USER | Đổi trạng thái todo |
| DELETE | `/api/todos/{id}` | USER | Xóa todo |

---

## 13. Backend Package Structure

```txt
src/main/java/com/nhan/todoapi
├── config/
│   └── SecurityConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   └── TodoController.java
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── LogoutRequest.java
│   │   ├── TodoCreationRequest.java
│   │   └── TodoUpdateRequest.java
│   │
│   └── response/
│       ├── ApiResponse.java
│       ├── UserResponse.java
│       ├── AuthenticationResponse.java
│       └── TodoResponse.java
│
├── entity/
│   ├── User.java
│   ├── Todo.java
│   ├── RefreshToken.java
│   ├── Role.java
│   └── Priority.java
│
├── exception/
│   ├── AppException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
│
├── repository/
│   ├── UserRepository.java
│   ├── TodoRepository.java
│   └── RefreshTokenRepository.java
│
├── service/
│   ├── AuthenticationService.java
│   ├── RefreshTokenService.java
│   ├── UserService.java
│   └── TodoService.java
│
└── security/
    ├── JwtService.java
    └── JwtAuthenticationFilter.java
```

---

## 14. Frontend Structure Gợi Ý

```txt
src/
├── api/
│   └── apiClient.js
│
├── services/
│   ├── authService.js
│   └── todoService.js
│
├── utils/
│   └── tokenStorage.js
│
├── stores/
│   └── authStore.js
│
├── pages/
│   ├── LoginPage.jsx
│   ├── RegisterPage.jsx
│   └── TodoPage.jsx
│
└── App.jsx
```

---

## 15. DTO Checklist

### Request DTO

```txt
RegisterRequest
LoginRequest
RefreshTokenRequest
LogoutRequest
TodoCreationRequest
TodoUpdateRequest
```

### Response DTO

```txt
ApiResponse<T>
UserResponse
AuthenticationResponse
TodoResponse
```

### AuthenticationResponse

```java
private String accessToken;
private String refreshToken;
private String tokenType;
private Long expiresIn;
private boolean authenticated;
```

### RefreshTokenRequest

```java
private String refreshToken;
```

### LogoutRequest

```java
private String refreshToken;
```

---

## 16. Repository Checklist

### RefreshTokenRepository

```java
Optional<RefreshToken> findByToken(String token);

List<RefreshToken> findAllByUser(User user);
```

Có thể thêm nếu cần:

```java
void deleteByUser(User user);
```

---

## 17. Service Checklist

### RefreshTokenService

Nên có các method:

```txt
createRefreshToken(User user)
validateRefreshToken(String token)
revokeRefreshToken(String token)
rotateRefreshToken(String oldToken, User user)
```

### AuthenticationService

Nên có các method:

```txt
login(LoginRequest request)
refreshToken(RefreshTokenRequest request)
logout(LogoutRequest request)
```

---

## 18. Error Code

| Code | Tên lỗi | HTTP | Khi nào dùng |
|---|---|---|---|
| 1000 | Success | 200/201 | Request thành công |
| 1001 | Invalid input | 400 | Validation fail |
| 1002 | User existed | 400 | Username hoặc email trùng |
| 1003 | Unauthenticated | 401 | Sai tài khoản/mật khẩu hoặc chưa login |
| 1004 | Access denied | 403 | Không có quyền |
| 1005 | Not found | 404 | Không tìm thấy dữ liệu |
| 1006 | Invalid token | 401 | Token sai format hoặc không hợp lệ |
| 1007 | Access token expired | 401 | Access token hết hạn |
| 1008 | Refresh token expired | 401 | Refresh token hết hạn |
| 1009 | Refresh token revoked | 401 | Refresh token đã logout hoặc bị rotate |
| 9999 | Uncategorized exception | 500 | Lỗi chưa phân loại |

---

## 19. Implementation Roadmap

| Phase | Tên phase | Việc cần làm | Kết quả kiểm tra |
|---|---|---|---|
| 1 | Backend JWT foundation | JwtService tạo access/refresh token | Generate được 2 loại token |
| 2 | Backend RefreshToken DB | Tạo RefreshToken entity/repository | DB có bảng refresh_tokens |
| 3 | Backend Login update | Login trả 2 token | Login đúng trả đủ token |
| 4 | Backend Refresh API | Check token, rotate token | Token cũ bị revoke, nhận token mới |
| 5 | Backend Logout API | Revoke refresh token | Logout xong token không dùng lại được |
| 6 | Backend Protected route test | Filter chỉ đọc Bearer accessToken | Todo/User API chạy bằng accessToken |
| 7 | Frontend Token storage | Lưu accessToken/refreshToken | Client có token sau login |
| 8 | Frontend Bearer request | Gắn Authorization header | Gọi protected API thành công |
| 9 | Frontend Auto refresh | 401 thì refresh và retry | User không bị logout khi access token hết hạn |
| 10 | Validation & test | Test toàn bộ luồng | Đạt tiêu chí nghiệm thu |

---

## 20. Thứ tự code khuyến nghị

```txt
1. Backend: kiểm tra JwtService hiện tại
2. Backend: tạo generateAccessToken()
3. Backend: tạo generateRefreshToken()
4. Backend: tạo RefreshToken entity
5. Backend: tạo RefreshTokenRepository
6. Backend: tạo RefreshTokenService.createRefreshToken()
7. Backend: update AuthenticationResponse
8. Backend: update AuthenticationService.login()
9. Backend: test login bằng Postman
10. Backend: tạo RefreshTokenRequest
11. Backend: implement AuthenticationService.refreshToken()
12. Backend: check DB token tồn tại, revoked=false, chưa expired
13. Backend: revoke token cũ và tạo token mới
14. Backend: thêm AuthController.refreshToken()
15. Backend: test rotate refresh token
16. Backend: tạo LogoutRequest
17. Backend: implement AuthenticationService.logout()
18. Backend: thêm AuthController.logout()
19. Backend: test logout revoke refresh token
20. Backend: kiểm tra JwtAuthenticationFilter chỉ đọc Bearer accessToken
21. Frontend: tạo tokenStorage
22. Frontend: update authService.login()
23. Frontend: update apiClient gắn Authorization: Bearer accessToken
24. Frontend: update apiClient xử lý 401 access token expired
25. Frontend: gọi authService.refreshToken()
26. Frontend: lưu accessToken mới + refreshToken mới
27. Frontend: retry request cũ
28. Frontend: nếu refresh fail thì clear token và chuyển về login
29. Test end-to-end
```

---

## 21. Frontend Token Storage

Ví dụ file:

```txt
src/utils/tokenStorage.js
```

Nên có các hàm:

```js
export const getAccessToken = () => localStorage.getItem("accessToken");

export const setAccessToken = (token) => {
  localStorage.setItem("accessToken", token);
};

export const getRefreshToken = () => localStorage.getItem("refreshToken");

export const setRefreshToken = (token) => {
  localStorage.setItem("refreshToken", token);
};

export const clearTokens = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
};
```

---

## 22. Frontend API Client Flow

File gợi ý:

```txt
src/api/apiClient.js
```

Request interceptor:

```txt
Lấy accessToken
Nếu có token thì gắn Authorization: Bearer <accessToken>
```

Response interceptor:

```txt
Nếu API trả 401 do access token expired:
→ gọi refresh-token API
→ lưu accessToken mới và refreshToken mới
→ retry request cũ
Nếu refresh fail:
→ clear token
→ chuyển về login
```

Quy tắc quan trọng:

```txt
Không dùng refreshToken làm Bearer token cho Todo/User API
Refresh token chỉ gửi trong body của /api/auth/refresh-token và /api/auth/logout
```

---

## 23. Test Plan

### 23.1 Login Test

- [ ] Login đúng trả `accessToken` và `refreshToken`.
- [ ] Login sai không trả token.
- [ ] Response có `tokenType = Bearer`.
- [ ] Refresh token được lưu vào bảng `refresh_tokens`.

### 23.2 Access Token Test

- [ ] Có access token gọi `/api/users/me` thành công.
- [ ] Có access token gọi `/api/todos` thành công.
- [ ] Không có token gọi API protected bị 401.
- [ ] Token sai format bị 401.
- [ ] Access token hết hạn bị 401.

### 23.3 Refresh Token Test

- [ ] Refresh token hợp lệ trả access token mới và refresh token mới.
- [ ] Refresh token không tồn tại DB bị 401.
- [ ] Refresh token hết hạn bị 401.
- [ ] Refresh token revoked bị 401.

### 23.4 Rotation Test

- [ ] Login nhận `R1`.
- [ ] Dùng `R1` gọi refresh-token.
- [ ] Backend revoke `R1`.
- [ ] Backend trả `R2`.
- [ ] Dùng lại `R1` bị 401.
- [ ] Dùng `R2` refresh thành công.

### 23.5 Logout Test

- [ ] Logout với refresh token hợp lệ thành công.
- [ ] Refresh token sau logout bị revoked.
- [ ] Sau logout, dùng refresh token đó bị 401.
- [ ] Frontend xóa access token và refresh token.

### 23.6 Frontend Auto Refresh Test

- [ ] Access token hết hạn.
- [ ] Frontend nhận 401.
- [ ] Frontend gọi refresh token API.
- [ ] Frontend lưu token mới.
- [ ] Frontend retry request cũ.
- [ ] Request cũ thành công.
- [ ] Nếu refresh fail thì frontend clear token và về login.

### 23.7 Todo Ownership Test

- [ ] User A chỉ xem được todo của User A.
- [ ] User A không xem/sửa/xóa được todo của User B.
- [ ] Sau khi refresh token, ownership vẫn hoạt động đúng.

---

## 24. Tiêu chí nghiệm thu

Update 1.02 được xem là đạt khi:

```txt
Login trả đủ accessToken và refreshToken
API protected dùng Authorization: Bearer accessToken
Refresh token được lưu DB dạng token string
Refresh token có revoked và expiredAt
Mỗi lần refresh sẽ rotate refresh token
Refresh token cũ bị revoked sau khi refresh
Logout revoke refresh token hiện tại
Frontend lưu accessToken và refreshToken
Frontend tự refresh khi access token hết hạn
Frontend retry request cũ sau khi refresh thành công
Refresh fail thì frontend clear token và về login
Todo ownership vẫn đúng
```

---

## 25. Demo Script

Thứ tự demo gợi ý:

```txt
1. Register user mới
2. Login user
3. Kiểm tra response có accessToken và refreshToken
4. Kiểm tra refresh token được lưu trong DB
5. Dùng accessToken gọi /api/users/me
6. Dùng accessToken gọi /api/todos
7. Gọi /api/auth/refresh-token bằng refreshToken R1
8. Kiểm tra DB: R1 bị revoked
9. Kiểm tra response trả accessToken mới và refreshToken R2
10. Dùng lại R1 để refresh, kết quả bị 401
11. Dùng R2 để refresh, kết quả thành công
12. Gọi logout bằng refreshToken hiện tại
13. Kiểm tra refresh token bị revoked
14. Frontend clear token và chuyển về login
```

---

## 26. Kết luận

Update 1.02 giúp hệ thống Todo App có cơ chế authentication tốt hơn bản login 1 token thông thường.

Điểm quan trọng nhất của update này là:

```txt
Access token dùng để gọi API protected
Refresh token dùng để xin token mới
Refresh token được lưu DB để backend kiểm soát
Mỗi lần refresh phải rotate token
Logout phải revoke refresh token
Frontend phải tự xử lý token expired
```

Bản này vẫn giữ mục tiêu MVP nên chưa hash refresh token và chưa dùng HttpOnly Cookie.  
Các phần đó có thể được nâng cấp ở những phiên bản bảo mật tiếp theo.

---

## 27. Báo Cáo Sửa Lỗi (Hotfix) Hệ Thống Backend
Quá trình kiểm tra và nghiệm thu đã phát hiện và khắc phục các điểm chưa đồng bộ ở Backend so với tài liệu thiết kế. Các thay đổi bao gồm:

1. **Chuẩn hoá API Đăng xuất (Logout)**:
   - Thay vì ném `accessToken` vào blacklist, API đã được sửa để nhận một `LogoutRequest` chứa `refreshToken` trong body của request.
   - Hàm xử lý ở `AuthenticationService` sẽ tra cứu Database và gán `revoked = true` cho `refreshToken` đó, chặn hoàn toàn việc tái sử dụng refreshToken sau khi đăng xuất.
2. **Cập nhật Security Config**:
   - Chỉnh sửa `PUBLIC_ENDPOINTS` trong `SecurityConfig`, đổi tên từ `/auth/refresh` thành `/auth/refresh-token` để frontend có thể gọi tự do API này khi `accessToken` bị hết hạn mà không bị chặn lỗi 401.
3. **Đồng bộ DTO**: 
   - Đã thêm field `refreshToken` cùng các lombok annotations vào file `LogoutRequest.java`.
