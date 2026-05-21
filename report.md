# Biên bản nghiệm thu (Review Report) Todo API

Dưới đây là biên bản nghiệm thu (Review Report) toàn bộ backend Todo API của bạn dưới góc độ một Senior Backend QA/Reviewer. Mình đã đọc code thực tế trong source của bạn (`AuthController`, `TodoService`, `AuthenticationService`, `SecurityConfig`, `User`, `GlobalExceptionHandler`, v.v.).

---

### A. Tổng quan đánh giá
- **Đạt bao nhiêu phần trăm:** Khoảng **90%** so với requirement MVP.
- **Có đủ điều kiện nghiệm thu MVP chưa:** **Đã đủ điều kiện** để nghiệm thu. Các luồng chính đều chạy được.
- **Những phần đạt:**
  - Cấu trúc project chia Layer (Controller - Service - Repository - DTO - Entity) rất sạch sẽ và chuẩn.
  - Áp dụng bảo mật dữ liệu rất tốt ở tầng Service: Các hàm CRUD của Todo đều lấy `currentUser` từ `SecurityContextHolder` và dùng `findByIdAndUser` thay vì `findById`. Đây là điểm sáng cực kỳ lớn để chống lỗi lộ dữ liệu (IDOR).
  - Dùng chuẩn `Nimbus JOSE JWT` và cấu trúc `oauth2ResourceServer` của Spring Security mới (thay vì tự viết filter) rất hiện đại và gọn gàng.
  - Các Entity ánh xạ DB chuẩn, đủ field, đã dùng BCrypt.
- **Những phần chưa đạt:**
  - Vẫn còn một rủi ro tiềm ẩn gây crash server do sử dụng `@Data` sai cách trong JPA.
  - Xử lý ném lỗi khi đăng nhập (Login) chưa đạt chuẩn bảo mật (bị dính lỗi User Enumeration).
  - Thiếu logic tự cập nhật `completedAt` khi chuyển status sang `DONE` như requirement.

---

### B. Lỗi nghiêm trọng cần sửa ngay

| Mức độ | Vấn đề | File/Class nghi ngờ | Vì sao sai | Cách sửa |
| :---: | :--- | :--- | :--- | :--- |
| **High** | Nguy cơ **StackOverflow** và lộ password trong RAM/Log. | `User.java`, `Todo.java` (thư mục entity) | Sử dụng `@Data` của Lombok trên Entity có quan hệ `@OneToMany` (`todos`) và `@ManyToOne` (`user`) sẽ sinh ra hàm `toString()`, `hashCode()` gọi chéo nhau liên tục, gây tràn bộ nhớ. Ngoài ra `toString()` sẽ in luôn cột `password` dạng raw ra ngoài console nếu bạn print object. | Xóa bỏ `@Data`. Thay bằng `@Getter` và `@Setter`. |
| **High** | Lỗi bảo mật User Enumeration khi Login. | `AuthenticationService.java` (hàm `login`) | Nếu user không tồn tại, bạn ném `USER_NOT_FOUND` (4001). Nếu mật khẩu sai, bạn ném `UNAUTHORIZED` (4005). Điều này giúp Hacker dò được trong hệ thống của bạn có tài khoản nào tồn tại hay không. | Gộp lại: Dù user không tồn tại hay sai mật khẩu, đều ném chung 1 lỗi: `new AppException(ErrorCode.INVALID_USERNAME_OR_PASSWORD)` (Mã 4004). |
| **Medium** | Không cấp Role (phân quyền) vào trong JWT Token. | `AuthenticationService.java` (hàm `generateToken`) | Bạn đã config `JwtAuthenticationConverter` trong `SecurityConfig` đọc claim `scope`. Tuy nhiên, lúc tạo JWT, đoạn code `claim("scope", buildScope(user))` đã bị bạn comment out mất. Spring Security sẽ không biết user này có role gì. | Bỏ comment đoạn `claim("scope", "ROLE_" + user.getRole().name())` vào trong hàm tạo `JWTClaimsSet`. |

---

### C. Lỗi vừa / cải thiện nên làm

| Mức độ | Vấn đề | Gợi ý sửa |
| :---: | :--- | :--- |
| **Low** | Bắt lỗi Validation chưa chi tiết (`MethodArgumentNotValidException`). | Trong `GlobalExceptionHandler`, khi client truyền thiếu title, email sai format... bạn đều trả chung 1 message cứng là `"Invalid request"`. Hãy lấy nguyên nhân thực tế bằng cách gọi: `exception.getBindingResult().getFieldError().getDefaultMessage()` để trả về cho người dùng (Ví dụ: *"Email is required"*). |
| **Low** | Thiếu logic tự động gán `completedAt` khi cập nhật Status. | Trong `TodoService.StatusUpdate()`, bạn chỉ gán `todo.setStatus(...)` và `updatedAt`. Nếu client gửi `status = DONE`, bạn nên check `if (status == DONE)` thì `setCompletedAt(LocalDateTime.now())`, ngược lại gán bằng `null`. |
| **Low** | Đặt tên method sai chuẩn convention của Java. | Trong `TodoController` và `TodoService`, method tên là `StatusUpdate` (viết hoa chữ S). Hàm trong Java bắt buộc phải viết camelCase (chữ cái đầu viết thường). | Sửa thành `statusUpdate()` hoặc `updateStatus()`. |

---

### D. Checklist nghiệm thu

| Hạng mục | Pass/Fail | Ghi chú |
| :--- | :---: | :--- |
| Cấu trúc project chia layer | **Pass** | Controller rất mỏng, logic nằm đúng Service. |
| Entity & Db Constraints | **Pass** | Có `@Column(unique=true)`, nullable=false. |
| JWT, Security Filter | **Pass** | Dùng OAuth2 Resource Server, config `SecurityFilterChain` chuẩn. |
| Không trả password ra API | **Pass** | `UserResponse` không chứa field password. |
| Register encode BCrypt | **Pass** | Đã encode trước khi lưu db. |
| Mọi API Todo chỉ trả dữ liệu của User đang đăng nhập (Ownership) | **Pass** | Áp dụng triệt để `findByIdAndUser` và `findAllByUser`. Code cực tốt. |
| Bắt Validation / Global Exception | **Pass** | Cấu trúc bắt lỗi đầy đủ. Cần làm cho message trả về chi tiết hơn. |

---

### E. Test case Postman/cURL (Danh sách Test tự động)

| Test Case Name | Method + Endpoint | Mẫu Body (JSON) | Header | Expected HTTP Status | Expected DB Effect / Response |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Register Success** | `POST /api/auth/register` | `{"username":"nhan","email":"n@mail.com","password":"123"}` | None | 200 OK | Ghi 1 dòng vào `users`, password đã hash BCrypt, role là USER. |
| **2. Register Dup Email** | `POST /api/auth/register` | `{"username":"nhan2","email":"n@mail.com","password":"123"}`| None | 400 Bad Req | Lỗi `EMAIL_ALREADY_EXISTS`. |
| **3. Login Success** | `POST /api/auth/login` | `{"username":"nhan","password":"123"}` | None | 200 OK | Trả về chuỗi JWT Token, `authenticated = true`. |
| **4. Login Wrong Pass** | `POST /api/auth/login` | `{"username":"nhan","password":"abc"}` | None | 401 Unauthorized | Báo lỗi đăng nhập thất bại. |
| **5. Call API w/o Token** | `GET /api/todos` | None | None | 401 Unauthorized | Spring Security chặn trực tiếp. |
| **6. Create Todo** | `POST /api/todos` | `{"title":"Học Spring","description":"Làm Todo API"}` | `Authorization: Bearer <token>` | 200 OK | Ghi vào `todos`, `user_id` map đúng với ID của User "nhan". |
| **7. Get My Todos** | `GET /api/todos` | None | `Authorization: Bearer <token>` | 200 OK | Trả về Array chứa các task của user đó. |
| **8. Try Get Other's Todo**| `GET /api/todos/99` (ID của user khác) | None | `Authorization: Bearer <token>` | 404 Not Found | Trả về `TODO_NOT_FOUND` (4007) do không match được currentUser. |
| **9. Update Status Todo** | `PATCH /api/todos/1/status`| `{"status":"DONE"}` | `Authorization: Bearer <token>` | 200 OK | Cột `status` trong DB thành `DONE`, `updated_at` đổi. |

---

### F. Kết luận
- **Backend này PASS MVP hoàn toàn.** Mặc dù có một số lỗi nhưng kiến trúc tổng thể, đặc biệt là phần logic phân quyền sở hữu tài nguyên (Resource Ownership) bằng `SecurityContext` + `findByIdAndUser` được viết cực kỳ chặt chẽ và không có lỗ hổng.
- **Để hoàn thiện hơn ngay hôm nay:** Bạn hãy xóa ngay annotation `@Data` ra khỏi `User` và `Todo` để tránh sập app, sửa lại throw Exception ở hàm Login thành `4004`, và lấy DefaultMessage của Exception `MethodArgumentNotValidException` để trả về lỗi Validation chi tiết.
- **Giai đoạn tiếp theo nên làm:** Tích hợp tính năng Filter/Search, Phân trang (Pagination) vào hàm `findAllByUser` (truyền `Pageable`). Thêm bảng `Category` nếu thích.
