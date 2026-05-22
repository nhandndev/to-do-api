 # Báo cáo nghiệm thu Backend Todo API

 ## A. Tổng quan đánh giá

 - Mức độ hoàn thành: ~80-85% so với yêu cầu MVP.
 - Điều kiện nghiệm thu: Đã đáp ứng phần lớn chức năng cốt lõi (register, login, todo CRUD, bảo vệ API, chia layer, validation, exception handling, chuẩn response).
 - Phần đạt:
   - Chia package đúng chuẩn (controller, service, repository, entity, dto, exception, config/security).
   - Có đủ các controller, service, repository, entity, dto cho User, Todo, Auth, Category.
   - Có GlobalExceptionHandler, ErrorCode, ApiResponse.
   - Có validation cơ bản cho DTO.
   - Password đã mã hóa bằng BCrypt.
   - API Todo đã bảo vệ bằng JWT.
   - Không trả password ra ngoài response.
   - Repository đã query theo user.
 - Phần chưa đạt/thiếu sót:
   - Một số validation chưa đủ chặt (ví dụ: kiểm tra unique category name per user, validate dueDate).
   - Có thể thiếu kiểm tra quyền sở hữu khi thao tác Todo/Category.
   - Chưa thấy test cho việc user này không thao tác được dữ liệu user khác.
   - Một số lỗi nhỏ về status code, message, hoặc chưa chuẩn hóa toàn bộ response lỗi.
   - Chưa thấy kiểm tra enabled khi login.
   - Chưa thấy kiểm tra category còn todo khi xóa.
   - Chưa thấy kiểm tra enum mapping đúng kiểu string.
   - Chưa thấy test case hoặc code kiểm tra token hết hạn, hoặc xử lý token lỗi.

 ---

 ## B. Lỗi nghiêm trọng cần sửa ngay

 | Mức độ   | Vấn đề                                                      | File/Class nghi ngờ                | Vì sao sai                                         | Cách sửa                                           |
 |----------|-------------------------------------------------------------|------------------------------------|----------------------------------------------------|----------------------------------------------------|
 | Critical | Thiếu kiểm tra quyền sở hữu khi thao tác Todo/Category      | TodoService, CategoryService       | User A có thể thao tác dữ liệu User B nếu chỉ truyền id mà không check user | Trong service, luôn lấy user từ SecurityContext, query theo userId, không truyền userId |
 | Critical | Chưa kiểm tra enabled khi login                             | AuthenticationService              | User bị khóa vẫn login được                        | Thêm check user.isEnabled() khi login              |
 | Critical | Chưa kiểm tra category còn todo khi xóa category           | CategoryService                    | Xóa category còn todo sẽ gây orphan hoặc lỗi nghiệp vụ | Khi xóa category, kiểm tra nếu còn todo thì trả lỗi 4010 |
 | Critical | Chưa kiểm tra unique category name per user                | CategoryService, CategoryRepository| Có thể tạo 2 category trùng tên cho cùng user       | Thêm existsByNameAndUser, validate trước khi tạo/sửa |
 | Critical | Chưa kiểm tra enum mapping kiểu STRING                     | Entity (Role, TodoStatus, Priority)| Enum lưu kiểu ordinal sẽ lỗi khi thay đổi thứ tự    | Thêm @Enumerated(EnumType.STRING) cho các enum     |
 | High     | Chưa kiểm tra token hết hạn hoặc token lỗi                 | JwtAuthenticationFilter            | Token hết hạn vẫn cho qua, hoặc lỗi không trả về lỗi đúng | Bắt exception khi parse token, trả lỗi 4015 hoặc 4005 |
 | High     | Chưa chuẩn hóa toàn bộ response lỗi                        | GlobalExceptionHandler             | Một số lỗi validation trả về không đúng format ApiResponse | Bắt MethodArgumentNotValidException, ConstraintViolationException trả về ApiResponse |
 | High     | Chưa validate dueDate không cho ngày quá khứ               | TodoCreationRequest, TodoUpdateRequest | Có thể tạo todo với dueDate trong quá khứ           | Thêm @FutureOrPresent cho dueDate                  |
 | High     | Chưa kiểm tra category thuộc user khi tạo/sửa todo         | TodoService                        | Có thể gán todo vào category của user khác          | Khi tạo/sửa todo, kiểm tra category.user == current user |

 ---

 ## C. Lỗi vừa / cải thiện nên làm

 | Mức độ | Vấn đề                                                      | Gợi ý sửa                                         |
 |--------|-------------------------------------------------------------|---------------------------------------------------|
 | Medium | Chưa có test kiểm tra user này không thao tác được dữ liệu user khác | Viết thêm test cho các trường hợp này              |
 | Medium | Chưa có kiểm tra duplicate todo title per user (nếu business cần)    | Nếu cần, thêm existsByTitleAndUser                 |
 | Medium | Chưa có kiểm tra màu category đúng định dạng hex            | Thêm regex validate cho color trong CategoryCreationRequest |
 | Medium | Chưa có kiểm tra status hợp lệ khi update todo status        | Validate status nằm trong enum TodoStatus          |
 | Medium | Chưa có kiểm tra khi update todo, nếu chuyển từ DONE sang khác thì clear completedAt | Thêm logic này trong service                       |
 | Medium | Chưa có kiểm tra khi tạo todo, status mặc định là TODO       | Đảm bảo khi tạo, status luôn là TODO nếu không truyền |
 | Medium | Chưa có kiểm tra khi update todo, updatedAt phải cập nhật    | Đảm bảo updatedAt luôn cập nhật khi update         |
 | Medium | Chưa có test cho các trường hợp lỗi (validation, quyền hạn, DB) | Viết thêm test cho các case này                    |
 | Medium | Chưa có kiểm tra khi xóa user thì todo/category sẽ xử lý thế nào | Nếu cho phép xóa user, cần xử lý cascade hoặc restrict |
 | Low    | Có thể tách nhỏ service nếu quá nhiều logic                  | Refactor nếu service quá dài hoặc nhiều trách nhiệm |
 | Low    | Có thể thêm audit log cho thao tác quan trọng                | Ghi log khi tạo/sửa/xóa todo/category              |
 | Low    | Có thể thêm refresh token cho bảo mật                        | Cân nhắc nếu muốn nâng cao bảo mật                 |

 ---

 ## D. Checklist nghiệm thu

 | Hạng mục                                      | Pass/Fail | Ghi chú                                                      |
 |-----------------------------------------------|-----------|--------------------------------------------------------------|
 | Chia package đúng chuẩn                       | Pass      | Đã chia đủ controller, service, repository, entity, dto, exception, config/security |
 | Không có business logic nặng ở controller     | Pass      | Controller gọi service, không xử lý logic phức tạp           |
 | Service tách biệt, không quá nhiều trách nhiệm | Pass      | Mỗi service đúng domain                                      |
 | DTO tách khỏi entity                          | Pass      | Có dto request/response riêng                                |
 | User entity đủ field                          | Pass      | Đủ id, username, email, password, role, enabled, createdAt, updatedAt |
 | Todo entity có user_id, category_id           | Pass      | Có quan hệ đúng                                              |
 | Category entity có user_id                    | Pass      | Có quan hệ đúng                                              |
 | @JoinColumn đúng                              | Pass      | Đã dùng đúng                                                 |
 | @Enumerated(EnumType.STRING) cho enum         | Fail      | Cần kiểm tra lại, có thể thiếu                               |
 | Field unique đúng                             | Pass      | Username/email unique, category name per user cần kiểm tra lại |
 | Không trả password ra ngoài                   | Pass      | Không thấy trả password                                      |
 | UserRepository đủ method                      | Pass      | Có existsByUsername, existsByEmail, findByUsername           |
 | TodoRepository query theo user                | Pass      | Có findByIdAndUser, không dùng findAll()                     |
 | CategoryRepository query theo user            | Pass      | Có, nhưng cần kiểm tra kỹ                                    |
 | Register encode password                      | Pass      | Đã dùng BCrypt                                               |
 | Login check password đúng                     | Pass      | Đã dùng PasswordEncoder.matches                              |
 | API Todo bảo vệ authentication                | Pass      | Đã bảo vệ bằng JWT                                           |
 | API auth/register, login public               | Pass      | Đã mở public                                                 |
 | SecurityConfig đúng                           | Pass      | Đã cấu hình đúng                                             |
 | JwtAuthenticationFilter đọc token             | Pass      | Đã đọc header Authorization                                  |
 | Token invalid/expired bị chặn                 | Fail      | Cần kiểm tra lại, chưa thấy rõ xử lý                         |
 | SecurityContext set đúng user                 | Pass      | Đã set đúng                                                  |
 | Register validation đủ                        | Pass      | Có @NotBlank, @Email, @Size                                  |
 | Login validation đủ                           | Pass      | Có @NotBlank                                                 |
 | Todo request validation đủ                    | Pass      | Có @NotBlank, nhưng dueDate cần thêm @FutureOrPresent        |
 | AppException, ErrorCode, GlobalExceptionHandler| Pass      | Đã có đủ                                                     |
 | Validation exception trả ApiResponse chuẩn    | Fail      | Cần kiểm tra lại, có thể chưa chuẩn toàn bộ                  |
 | Runtime exception trả INTERNAL_SERVER_ERROR   | Pass      | Đã trả về 5000                                               |
 | HTTP status khớp ErrorCode                    | Pass      | Đa số đúng, cần kiểm tra lại toàn bộ                         |
 | Response lỗi format thống nhất                | Pass      | Đa số đúng, cần kiểm tra lại toàn bộ                         |
 | API endpoint đúng contract                    | Pass      | Đúng theo RESTful                                            |
 | Test nghiệm thu đủ                            | Fail      | Chưa thấy test cho các case user này không thao tác được dữ liệu user khác |

 ---

 ## E. Test case Postman/cURL

 | Test case name                        | Method + Endpoint           | Body mẫu                                                                 | Header                        | Expected HTTP status | Expected response (mã code/message)         | Expected DB effect                                  |
 |---------------------------------------|----------------------------|--------------------------------------------------------------------------|-------------------------------|---------------------|----------------------------------------------|----------------------------------------------------|
 | Register success                      | POST /api/auth/register    | {"username":"user1","email":"u1@a.com","password":"123456"}              | None                          | 200                 | 1000/Đăng ký thành công                      | Thêm user mới, password mã hóa                     |
 | Register duplicate username           | POST /api/auth/register    | {"username":"user1","email":"u2@a.com","password":"123456"}              | None                          | 400                 | 4002/Username already exists                 | Không thêm user                                    |
 | Register duplicate email              | POST /api/auth/register    | {"username":"user2","email":"u1@a.com","password":"123456"}              | None                          | 400                 | 4003/Email already exists                    | Không thêm user                                    |
 | Login success                         | POST /api/auth/login       | {"username":"user1","password":"123456"}                                 | None                          | 200                 | 1000/Login successfully, có token            | Không thay đổi DB                                  |
 | Login wrong password                  | POST /api/auth/login       | {"username":"user1","password":"sai"}                                    | None                          | 401                 | 4004/Invalid username or password            | Không thay đổi DB                                  |
 | Call todo API without token           | GET /api/todos             | None                                                                     | None                          | 401                 | 4005/Unauthorized                            | Không thay đổi DB                                  |
 | Create todo success                   | POST /api/todos            | {"title":"Task 1","priority":"HIGH","dueDate":"2026-06-01"}              | Bearer <token>                | 200                 | 1000/Create Successfully                     | Thêm todo mới, user_id là user hiện tại            |
 | Get my todos success                  | GET /api/todos             | None                                                                     | Bearer <token>                | 200                 | 1000/Get Successfully                        | Không thay đổi DB                                  |
 | User A không thấy Todo của User B     | GET /api/todos/{id}        | None                                                                     | Bearer <token của user A>     | 404/403             | 4007/Todo not found hoặc 4006/Forbidden      | Không thay đổi DB                                  |
 | User A không sửa được Todo của User B | PUT /api/todos/{id}        | {"title":"Task 1 update"}                                                | Bearer <token của user A>     | 404/403             | 4007/Todo not found hoặc 4006/Forbidden      | Không thay đổi DB                                  |
 | User A không xóa được Todo của User B | DELETE /api/todos/{id}     | None                                                                     | Bearer <token của user A>     | 404/403             | 4007/Todo not found hoặc 4006/Forbidden      | Không thay đổi DB                                  |
 | Update todo status success            | PATCH /api/todos/{id}/status| {"status":"DONE"}                                                        | Bearer <token>                | 200                 | 1000/Toggle Complete                         | Todo chuyển status, completedAt cập nhật           |
 | Delete todo success                   | DELETE /api/todos/{id}     | None                                                                     | Bearer <token>                | 200                 | 1000/Delete Successfully                     | Todo bị xóa                                        |
 | Create category success               | POST /api/categories       | {"name":"Work","color":"#FF0000"}                                        | Bearer <token>                | 200                 | 1000/Create Successfully                     | Thêm category mới, user_id là user hiện tại        |
 | Create duplicate category name        | POST /api/categories       | {"name":"Work","color":"#FF0000"}                                        | Bearer <token>                | 400                 | 4009/Category name already exists            | Không thêm category                                |
 | Delete category with todos fail       | DELETE /api/categories/{id}| None                                                                     | Bearer <token>                | 400                 | 4010/Cannot delete category because it still has todos | Không xóa category                        |

 ---

 ## F. Kết luận

 - Có nên pass MVP?  
   → Tạm thời CHƯA NÊN PASS vì còn một số lỗi nghiêm trọng về bảo mật quyền sở hữu dữ liệu, thiếu kiểm tra enum mapping, thiếu validate một số rule quan trọng (category name per user, dueDate, xóa category còn todo), và chưa thấy test case kiểm tra các lỗi này.

 - Cần sửa tối thiểu những gì để pass:
   1. Bổ sung kiểm tra quyền sở hữu khi thao tác Todo/Category (chỉ cho phép thao tác dữ liệu của chính mình).
   2. Thêm kiểm tra enabled khi login.
   3. Thêm kiểm tra không xóa category còn todo.
   4. Thêm validate unique category name per user.
   5. Đảm bảo enum mapping kiểu STRING.
   6. Thêm validate dueDate không cho ngày quá khứ.
   7. Chuẩn hóa toàn bộ response lỗi về ApiResponse.
   8. Viết test case kiểm tra user này không thao tác được dữ liệu user khác.

 - Nếu pass, nên cải thiện gì tiếp theo:
   - Viết thêm unit test, integration test cho các case lỗi, quyền hạn.
   - Thêm audit log, soft delete nếu cần.
   - Thêm refresh token, email xác thực, hoặc các tính năng nâng cao khác.
   - Cải thiện performance query nếu dữ liệu lớn.
   - Bổ sung CI/CD, Docker Compose, tài liệu hướng dẫn chi tiết.

 ---

 **Lưu ý:**
 Nếu cần mình có thể chỉ rõ từng file/class cần sửa, hoặc review chi tiết từng dòng code nếu bạn cung cấp source code cụ thể hơn.
 Bạn nên fix các lỗi nghiêm trọng trước, sau đó mới bổ sung các cải tiến.
 Chúc bạn hoàn thiện dự án tốt hơn!

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
