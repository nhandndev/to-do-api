# Báo cáo Nghiệm Thu Backend Todo API
**Reviewer:** Senior Backend QA Engineer | **Date:** 22/05/2026 | **Status:** CHƯA PASS MVP (Cần sửa 7 lỗi critical)

---

## A. Tổng quan đánh giá

- **Mức độ hoàn thành:** 70-75% so với yêu cầu MVP
- **Điều kiện nghiệm thu:** **CHƯA ĐỦ** - Còn 7 lỗi critical ảnh hưởng đến chức năng cốt lõi
- **Phần đạt (✅):**
  - Chia layer đúng chuẩn (controller, service, repository, entity, dto, exception, config)
  - Entity design cơ bản đúng (User-Todo relationship, @Enumerated(EnumType.STRING))
  - Password mã hóa BCrypt trong register
  - API Todo bảo vệ bằng JWT
  - Repository query theo user (findByIdAndUser)
  - GlobalExceptionHandler, ErrorCode, ApiResponse format chuẩn
  - Không trả password ra response

- **Phần CHƯA đạt (❌):**
  - **7 lỗi critical** cần sửa ngay (chi tiết phần B)
  - **5 lỗi medium** cần cải thiện (chi tiết phần C)
  - Validation input thiếu chặt
  - Token expiration không đúng setting

---

## B. Lỗi nghiêm trọng cần sửa ngay

| # | Mức độ | Vấn đề | File/Class | Vì sao sai | Cách sửa |
|---|---------|--------|-----------|-----------|---------|
| 1 | **Critical** | Login không check `enabled` | AuthenticationService.java:60 | User bị disable (enabled=false) vẫn login được, vi phạm business rule | Thêm code: `if (!user.getEnabled()) throw new AppException(ErrorCode.USER_DISABLED);` trước khi trả token |
| 2 | **Critical** | `updateToDo()` không update field | TodoService.java:45 | Hàm chỉ set updatedAt, không set title/description/status/dueDate từ request. **Update endpoint hoàn toàn không hoạt động** | Thêm: `todo.setTitle(request.getTitle()); todo.setDescription(request.getDescription()); todo.setStatus(request.getStatus()); todo.setDueDate(request.getDueDate());` |
| 3 | **Critical** | UserController endpoint sai | UserController.java:14 | GET /users/me mapped thành **POST** /users/me, wrong HTTP method | Đổi `@PostMapping("/me")` thành `@GetMapping("/me")` |
| 4 | **Critical** | TodoCreationRequest validation sai | TodoCreationRequest.java:11 | Size constraint `min=1` nhưng requirement yêu cầu min=3. `@NotBlank` thiếu cho title | Thêm `@NotBlank` và đổi `@Size(min = 3, max = 100)` |
| 5 | **Critical** | TodoUpdateRequest có JPA annotations | TodoUpdateRequest.java:7-9 | DTO request **không nên** có `@Id`, `@GeneratedValue` - đây là entity concerns | Xóa 3 dòng: `@Id`, `@GeneratedValue`, `private Long id;` |
| 6 | **Critical** | Token expiration quá lâu | AuthenticationService.java:73 | Expiration = 9999 giây (~2.7 giờ) nhưng requirement = 1 giờ (3600 giây) | Đổi `.plus(9999, ChronoUnit.SECONDS)` thành `.plus(3600, ChronoUnit.SECONDS)` |
| 7 | **Critical** | AuthenticationRequest không validate | AuthenticationRequest.java | Không có @NotBlank/@Size cho username/password | Thêm: `@NotBlank String username;` và `@NotBlank String password;` |

---

## C. Lỗi vừa / cải thiện nên làm

| # | Mức độ | Vấn đề | Gợi ý sửa |
|---|---------|--------|----------|
| 1 | **Medium** | TodoResponse thiếu `updatedAt` | Thêm field: `LocalDateTime updatedAt;` vào TodoResponse.java line 9 |
| 2 | **Medium** | TodoCreationRequest không validate @NotBlank | Thêm `@NotBlank(message = "Title is required")` trên field title |
| 3 | **Medium** | Login endpoint không validate request | Thêm `@Valid` annotation trên AuthenticationRequest trong AuthController.login() |
| 4 | **Medium** | UserRepository thiếu findByEmail() | Thêm method: `Optional<User> findByEmail(String email);` vào UserRepository.java |
| 5 | **Medium** | TodoResponse thiếu status field | Thêm field `TodoStatus status;` - cần lấy status từ todo entity |
| 6 | **Medium** | UpdateTodo không validate input | Thêm `@Valid` trên TodoUpdateRequest parameter trong TodoController.updateToDo() |
| 7 | **Medium** | GlobalExceptionHandler không bắt MethodArgumentNotValidException đầy đủ | Cải thiện message validation - lấy first error message thay vì generic "Invalid request" |
| 8 | **Medium** | Mapper không rõ ràng trong Todo | TodoResponse thiếu field và mapper không complete |

---

## D. Checklist Nghiệm Thu

| Hạng mục | Pass/Fail | Ghi chú |
|---------|-----------|--------|
| **Cấu trúc Project** | ✅ Pass | Đã chia đúng layer: controller, service, repository, entity, dto, exception, config |
| **Package Organization** | ✅ Pass | Mỗi layer có package riêng, không có misplaced class |
| **Entity Design** | ✅ Pass | User có đủ field, Todo có ManyToOne relationship, @Enumerated(EnumType.STRING) đúng |
| **Entity Nullable Constraint** | ⚠️ Partial | User.username, email, password: OK. Todo.title: OK. Nhưng Todo còn một số field nên explicit `nullable=false` |
| **Unique Constraint** | ✅ Pass | User.username unique=true, User.email unique=true |
| **Foreign Key Constraint** | ✅ Pass | Todo.user_id @JoinColumn(name="user_id", nullable=false) đúng |
| **Repository Methods** | ✅ Pass | UserRepository: existsByUsername, existsByEmail, findByUsername OK. TodoRepository: findByIdAndUser, findAllByUser OK |
| **No findAll() misuse** | ✅ Pass | TodoRepository không dùng findAll, chỉ dùng findAllByUser(currentUser) |
| **Register BCrypt** | ✅ Pass | passwordEncoder.encode() được gọi đúng |
| **Register Validation** | ⚠️ Fail | RegisterRequest: size min=1 sai, không có @NotBlank cho password, không validate confirmPassword |
| **Login Password Check** | ✅ Pass | passwordEncoder.matches() đúng |
| **Login Check Enabled** | ❌ **FAIL** | **CRITICAL:** Không check user.enabled, user bị khóa vẫn login |
| **Login Error Message** | ✅ Pass | Trả lỗi chung 4004 khi sai username/password |
| **API Todo Require Auth** | ✅ Pass | /todos/** đều yêu cầu authentication |
| **API Auth Public** | ✅ Pass | /auth/register, /auth/login permitAll |
| **SecurityConfig FilterChain** | ✅ Pass | Cấu hình oauth2ResourceServer + JWT decoder đúng |
| **SecurityContext Current User** | ✅ Pass | Lấy username từ SecurityContextHolder.getContext() đúng |
| **ResponseFormat Unified** | ✅ Pass | Tất cả API trả ApiResponse<T>(code, message, result) đúng chuẩn |
| **HTTP Status Code** | ✅ Pass | ErrorCode có mapping đúng HttpStatus |
| **Create Todo Ownership** | ✅ Pass | Gán todo.user = currentUser, không cho client truyền userId |
| **Get Todo Ownership** | ✅ Pass | Dùng findByIdAndUser(id, currentUser) để kiểm tra quyền |
| **Update Todo Ownership** | ✅ Pass | findByIdAndUser đúng, NHƯNG **hàm không update field (CRITICAL BUG)** |
| **Delete Todo Ownership** | ✅ Pass | findByIdAndUser đúng, delete chỉ user của mình |
| **Controller Business Logic** | ⚠️ Partial | Có một vài logic nhỏ trong controller (ok), nhưng chủ yếu ở service |
| **DTO vs Entity** | ⚠️ Fail | TodoUpdateRequest **sai** - có @Id, @GeneratedValue (JPA concerns) |
| **GET /users/me Endpoint** | ❌ **FAIL** | **Mapped thành POST** thay vì GET |
| **UPDATE /todos/{id} Endpoint** | ❌ **FAIL** | **Không update được** - hàm updateToDo lỗi nặng |
| **Validation Exception Handler** | ⚠️ Partial | MethodArgumentNotValidException được catch nhưng message generic |
| **No Password in Response** | ✅ Pass | UserResponse không có password field |
| **Test Coverage** | ❌ Fail | Chưa thấy test code |

---

## E. Test Case Nghiệm Thu (Postman/cURL)

| # | Test Case | Method + Endpoint | Request Body | Header | Expected 200 Status | Expected Response | DB Effect |
|---|-----------|------------------|--------------|--------|-------------------|-------------------|-----------|
| 1 | Register Success | POST /auth/register | {"username":"user1","email":"u1@test.com","password":"123456"} | - | 200 OK | code=1000, UserResponse (no password) | User tạo mới, password hash |
| 2 | Register Duplicate Username | POST /auth/register | {"username":"user1",...} | - | 400 | code=4002 "Username already exists" | Không tạo user |
| 3 | Register Duplicate Email | POST /auth/register | {...,"email":"u1@test.com"} | - | 400 | code=4003 "Email already exists" | Không tạo user |
| 4 | Register Invalid Email | POST /auth/register | {"username":"user2","email":"invalid",...} | - | 400 | code=4000 "Invalid request" | Không tạo user |
| 5 | Register Password Too Short | POST /auth/register | {...,"password":"123"} | - | 400 | code=4000 "Invalid request" | Không tạo user |
| 6 | Login Success | POST /auth/login | {"username":"user1","password":"123456"} | - | 200 OK | code=1000, token + authenticated=true | - |
| 7 | Login Wrong Password | POST /auth/login | {"username":"user1","password":"wrong"} | - | 401 | code=4004 "Invalid username or password" | - |
| 8 | Login User Not Found | POST /auth/login | {"username":"notexist","password":"123456"} | - | 401 | code=4004 "Invalid username or password" | - |
| 9 | **Login User Disabled (FAIL NOW)** | POST /auth/login | (user.enabled=false login) | - | **401 Expected** | **code=4005 Unauthorized** | **Current BUG: Returns token!** |
| 10 | Get My Info | GET /users/me | - | Bearer {token} | 200 OK | code=1000, UserResponse | - |
| 11 | Get My Info No Token | GET /users/me | - | - | 401 | code=4005 "Unauthorized" | - |
| 12 | Create Todo Success | POST /todos | {"title":"Learn Java","description":"..."} | Bearer {token} | 200 OK | code=1000, TodoResponse | Todo created with user_id |
| 13 | Create Todo Empty Title | POST /todos | {"title":"","description":"..."} | Bearer {token} | 400 | code=4000 "Invalid request" | No todo created |
| 14 | Create Todo No Token | POST /todos | {...} | - | 401 | code=4005 "Unauthorized" | - |
| 15 | Get My Todos | GET /todos | - | Bearer {token} | 200 OK | code=1000, List<TodoResponse> | - |
| 16 | Get My Todos No Token | GET /todos | - | - | 401 | code=4005 "Unauthorized" | - |
| 17 | Get Todo Detail | GET /todos/1 | - | Bearer {token} | 200 OK | code=1000, TodoResponse | - |
| 18 | Get Todo No Own (403) | GET /todos/99 | - | Bearer {token of user2} | 404 | code=4007 "Todo not found" | - |
| 19 | **Update Todo Success (FAIL NOW)** | PUT /todos/1 | {"title":"Updated","status":"IN_PROGRESS"} | Bearer {token} | **200 Expected** | **code=1000, updated TodoResponse** | **Current BUG: Only updatedAt changes!** |
| 20 | Update Todo No Own | PUT /todos/99 | {...} | Bearer {token2} | 404 | code=4007 "Todo not found" | - |
| 21 | Update Todo Status | PATCH /todos/1/status | {"status":"DONE"} | Bearer {token} | 200 OK | code=1000, status=DONE | Todo.completedAt set |
| 22 | Update Status Invalid | PATCH /todos/1/status | {"status":"INVALID"} | Bearer {token} | 400 | code=4000 "Invalid request" | - |
| 23 | Delete Todo | DELETE /todos/1 | - | Bearer {token} | 200 OK | code=1000 "Delete Successfully" | Todo deleted from DB |
| 24 | Delete Todo No Own | DELETE /todos/99 | - | Bearer {token2} | 404 | code=4007 "Todo not found" | - |
| 25 | Security: Todo API = Require Auth | GET /todos | - | - | 401 | code=4005 "Unauthorized" | - |
| 26 | User A Tidak Thấy Todo User B | GET /todos (user A) | - | Bearer {tokenA} | 200 OK | List chỉ có todo của A | todoB không hiển thị |
| 27 | User A Không Sửa Todo User B | PUT /todos/{idB} (user A) | {...} | Bearer {tokenA} | 404 | code=4007 "Todo not found" | todoB không thay đổi |
| 28 | User A Không Xóa Todo User B | DELETE /todos/{idB} | - | Bearer {tokenA} | 404 | code=4007 "Todo not found" | todoB không bị xóa |

**Note:** 
- Test case #9, 19 hiện tại đang FAIL vì lỗi code
- Tất cả test khác nên PASS sau khi fix

---

## F. Kết luận

### ❌ Có nên xem backend này là **PASS MVP**?

**Trả lời: KHÔNG, CHƯA PASS**

**Lý do:**
1. **7 lỗi critical** cần sửa ngay (lỗi login, update todo, endpoint sai, validation)
2. **Login khác không check enabled** - User bị khóa vẫn login được (hấp dẫn bảo mật)
3. **UpdateToDo completely broken** - Endpoint không cập nhật được dữ liệu
4. **GET /users/me mapped thành POST** - HTTP method sai

### 🔧 Cần sửa tối thiểu 7 lỗi critical (Ưu tiên cao):

**Priority 1 (Sửa trước):**
1. ✅ Fix updateToDo() - thêm update title/description/status/dueDate
2. ✅ Fix login - thêm check enabled
3. ✅ Fix UserController - đổi POST → GET /me
4. ✅ Fix TodoCreationRequest - thêm @NotBlank, @Size(min=3)

**Priority 2 (Sửa sau):**
5. ✅ Fix TodoUpdateRequest - xóa @Id, @GeneratedValue
6. ✅ Fix token expiration - 9999 → 3600 seconds
7. ✅ Fix AuthenticationRequest - thêm @NotBlank

**Effort:** ~2-3 giờ code + test

### ✅ Nếu fix xong 7 lỗi trên, nên cải thiện gì tiếp theo?

1. **Add validation:**
   - Validate email format trong RegisterRequest (đã có @Email)
   - Validate password min length 6
   - Validate dueDate không cho quá khứ

2. **Thêm features áp dụng:**
   - completedAt auto-set khi status=DONE
   - completedAt clear khi status change từ DONE → khác
   - Soft delete nếu cần (cascade delete vs soft delete)

3. **Cải thiện code:**
   - Thêm mapper (MapStruct) cho DTO ↔ Entity
   - Thêm unit test (AuthenticationServiceTest, TodoServiceTest)
   - Thêm integration test
   - Cụm lại exception handling - message validation chi tiết

4. **Security nâng cao:**
   - Add refresh token support
   - Implement logout/token blacklist
   - Rate limiting
   - Input sanitization

5. **DevOps:**
   - Thêm Docker/Docker Compose
   - Thêm CI/CD (GitHub Actions)
   - Thêm README chi tiết

---

## Đánh giá chung

Bạn đã có **nền tảng tốt** với layer structure chuẩn, security configuration đúng, exception handling ổn. Nhưng **7 bug critical** khiến backend **không thể nghiệm thu ngay**. Sau khi fix 2-3 giờ, project sẽ đạt chuẩn MVP và có thể demó cho khách hàng.

**Good luck! 🚀**
