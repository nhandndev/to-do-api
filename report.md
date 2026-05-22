# Báo cáo Nghiệm Thu Backend Todo API
**Reviewer:** Senior Backend QA Engineer | **Date:** 22/05/2026 | **Status:** ❌ CHƯA PASS MVP (Cần sửa 7 lỗi critical)

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
| 1 | **🔴 Critical** | Login không check `enabled` | **AuthenticationService.java:60** | User bị disable (enabled=false) vẫn login được, vi phạm business rule | **Thêm** trước return token: `if (!user.getEnabled()) { throw new AppException(ErrorCode.UNAUTHORIZED); }` |
| 2 | **🔴 Critical** | `updateToDo()` không update field | **TodoService.java:45-49** | Hàm chỉ set updatedAt, **không set title/description/status/dueDate**. **Update endpoint hoàn toàn không hoạt động** | **Thêm 4 dòng**: ```java todo.setTitle(request.getTitle()); todo.setDescription(request.getDescription()); todo.setStatus(request.getStatus()); todo.setDueDate(request.getDueDate()); ``` |
| 3 | **🔴 Critical** | UserController GET mapped sai | **UserController.java:14** | `@PostMapping("/me")` - **GET request mapped thành POST**, sai HTTP method | **Đổi** `@PostMapping` → `@GetMapping` |
| 4 | **🔴 Critical** | TodoCreationRequest validation sai | **TodoCreationRequest.java:11** | `@Size(min = 1)` nhưng requirement = min 3. **Thiếu @NotBlank** cho title | **Sửa**: Thêm `@NotBlank(message = "Title is required")` + đổi `min = 3` |
| 5 | **🔴 Critical** | TodoUpdateRequest có JPA annotations | **TodoUpdateRequest.java:7-9** | DTO request **không nên** có `@Id`, `@GeneratedValue` - đây là entity concern | **Xóa 3 dòng**: `@Id`, `@GeneratedValue`, `private Long id;` Giữ lại chỉ: title, description, status, dueDate |
| 6 | **🔴 Critical** | Token expiration quá lâu | **AuthenticationService.java:73** | Expiration = **9999 giây (~2.7 giờ)** nhưng requirement = **1 giờ (3600 giây)** | **Đổi**: `.plus(9999, ChronoUnit.SECONDS)` → `.plus(3600, ChronoUnit.SECONDS)` |
| 7 | **🔴 Critical** | AuthenticationRequest không validate | **AuthenticationRequest.java** | Không có `@NotBlank` cho username/password, client có thể gửi rỗng | **Thêm**: `@NotBlank(message = "Username is required")` trên username + password |

---

## C. Lỗi vừa / cải thiện nên làm

| # | Mức độ | Vấn đề | Gợi ý sửa |
|---|---------|--------|----------|
| 1 | 🟡 Medium | TodoResponse thiếu `updatedAt` field | Thêm `LocalDateTime updatedAt;` vào file **TodoResponse.java** - response phải có updatedAt |
| 2 | 🟡 Medium | TodoResponse thiếu `status` field | Thêm `TodoStatus status;` - client cần biết status của todo |
| 3 | 🟡 Medium | Login endpoint không validate | Thêm `@Valid` trên parameter `AuthenticationRequest` trong **AuthController.login()** |
| 4 | 🟡 Medium | Improve GlobalExceptionHandler message | Khi validation fail, extract first error message thay vì generic "Invalid request" |
| 5 | 🟡 Medium | UserRepository thiếu findByEmail | Thêm method: `Optional<User> findByEmail(String email);` (optional, dùng nếu có feature tìm kiếm) |

---

## D. Checklist Nghiệm Thu Chi Tiết

| Hạng mục | Pass/Fail | Ghi chú |
|---------|-----------|--------|
| **Cấu trúc Project** | ✅ PASS | Đã chia đúng layer: controller, service, repository, entity, dto, exception, config |
| **Entity User** | ✅ PASS | Đủ field: id, username, email, password (hash), role, enabled, createdAt, updatedAt |
| **Entity Todo** | ✅ PASS | Có quan hệ ManyToOne với User, @JoinColumn(name="user_id", nullable=false) đúng |
| **Enum Mapping** | ✅ PASS | @Enumerated(EnumType.STRING) cho Role, TodoStatus |
| **Password Encoding** | ✅ PASS | passwordEncoder.encode() được gọi đúng khi register |
| **Password Not in Response** | ✅ PASS | UserResponse không có password field |
| **UserRepository** | ✅ PASS | existsByUsername, existsByEmail, findByUsername - đủ |
| **TodoRepository** | ✅ PASS | findByIdAndUser, findAllByUser - query đúng theo user |
| **Register Validation** | ⚠️ FAIL | Size min=1 sai. Thiếu @NotBlank trên một số field |
| **Login Check Enabled** | ❌ FAIL | **CRITICAL:** Không kiểm tra user.enabled trước khi login |
| **Login Error Message** | ✅ PASS | Trả lỗi chung 4004 khi sai username/password |
| **API Auth Public** | ✅ PASS | /auth/register, /auth/login permitAll trong SecurityConfig |
| **API Todo Require Auth** | ✅ PASS | /todos/** đều yêu cầu authentication |
| **UserController Endpoint** | ❌ FAIL | **`@PostMapping("/me")` sai, phải là `@GetMapping`** |
| **SecurityConfig** | ✅ PASS | JwtDecoder, JwtAuthenticationConverter, oauth2ResourceServer cấu hình đúng |
| **SecurityContext** | ✅ PASS | Lấy current user từ SecurityContextHolder.getContext().getAuthentication().getName() đúng |
| **Create Todo Ownership** | ✅ PASS | Gán todo.user = currentUser, không cho client truyền userId |
| **Get Todo Ownership** | ✅ PASS | findByIdAndUser(id, currentUser) kiểm tra quyền |
| **Update Todo** | ❌ FAIL | **Hàm updateToDo() chỉ update updatedAt, không update title/description/status/dueDate** |
| **Delete Todo Ownership** | ✅ PASS | findByIdAndUser + delete - kiểm tra quyền đúng |
| **DTO vs Entity** | ❌ FAIL | **TodoUpdateRequest có @Id, @GeneratedValue** - DTO không nên có JPA annotations |
| **Mapper** | ⚠️ PARTIAL | TodoResponse thiếu updatedAt, status fields |
| **Response Format** | ✅ PASS | ApiResponse<T>(code, message, result) - format thống nhất |
| **HTTP Status Code** | ✅ PASS | ErrorCode mapping HttpStatus đúng |
| **GlobalExceptionHandler** | ⚠️ PARTIAL | Bắt được MethodArgumentNotValidException nhưng message chưa chi tiết |
| **Token Expiration** | ❌ FAIL | 9999 giây thay vì 3600 giây (1 giờ) |
| **Business Logic Layer** | ✅ PASS | Logic chủ yếu ở Service, controller chỉ gọi |

---

## E. Test Case Nghiệm Thu (Postman/cURL)

### Group 1: Authentication

| # | Test Case | Method + Endpoint | Body | Header | Expected Status | Expected Response | Note |
|---|-----------|-------------------|------|--------|-----------------|-------------------|------|
| 1 | Register Success | POST /auth/register | `{"username":"user1","email":"u1@test.com","password":"123456"}` | - | 200 | code=1000, UserResponse (no pwd) | ✅ PASS |
| 2 | Register Dup Username | POST /auth/register | `{"username":"user1","email":"u2@test.com",...}` | - | 400 | code=4002 | ✅ PASS |
| 3 | Register Dup Email | POST /auth/register | `{"username":"user2","email":"u1@test.com",...}` | - | 400 | code=4003 | ✅ PASS |
| 4 | Register Invalid Email | POST /auth/register | `{..."email":"invalid"...}` | - | 400 | code=4000 | Validation fail |
| 5 | Login Success | POST /auth/login | `{"username":"user1","password":"123456"}` | - | 200 | code=1000, token + auth=true | ✅ PASS |
| 6 | Login Wrong Password | POST /auth/login | `{"username":"user1","password":"wrong"}` | - | 401 | code=4004 | ✅ PASS |
| 7 | Login User Not Found | POST /auth/login | `{"username":"notexist",...}` | - | 401 | code=4004 | ✅ PASS |
| 8 | **Login User Disabled** | POST /auth/login | (user.enabled=false) | - | 401 | code=4005 Unauthorized | ❌ **FAIL - BUG:** Currently returns token |

### Group 2: User APIs

| # | Test Case | Method + Endpoint | Body | Header | Expected Status | Expected Response |
|---|-----------|-------------------|------|--------|-----------------|-------------------|
| 9 | Get My Info | **GET** /users/me | - | Bearer {token} | 200 | code=1000, UserResponse |
| 10 | Get My Info No Token | GET /users/me | - | - | 401 | code=4005 Unauthorized |
| 11 | **Get My Info Current** | POST /users/me | - | Bearer token | **Currently returns 200** | ❌ **FAIL - Wrong HTTP method** |

### Group 3: Todo CRUD

| # | Test Case | Method + Endpoint | Body | Header | Expected Status | Expected Response |
|---|-----------|-------------------|------|--------|-----------------|-------------------|
| 12 | Create Todo Success | POST /todos | `{"title":"Learn Java"}` | Bearer {token} | 200 | code=1000, TodoResponse |
| 13 | Create Todo Empty Title | POST /todos | `{"title":""}` | Bearer {token} | 400 | code=4000 Invalid request |
| 14 | Create Todo Min Size | POST /todos | `{"title":"AB"}` | Bearer {token} | 400 | code=4000 (size < 3) |
| 15 | Get My Todos | GET /todos | - | Bearer {token} | 200 | code=1000, List<TodoResponse> |
| 16 | Get Todo Detail | GET /todos/1 | - | Bearer {token} | 200 | code=1000, TodoResponse |
| 17 | Get Todo No Own | GET /todos/99 | - | Bearer {tokenB} | 404 | code=4007 Todo not found |
| 18 | **Update Todo** | PUT /todos/1 | `{"title":"Updated","status":"IN_PROGRESS"}` | Bearer {token} | 200 | **Should update title + status** |
| 19 | **Update Todo Current** | PUT /todos/1 | Same | Bearer {token} | **Currently 200** | ❌ **FAIL - Không update được field** |
| 20 | Update Status | PATCH /todos/1/status | `{"status":"DONE"}` | Bearer {token} | 200 | code=1000, status=DONE |
| 21 | Update Status Invalid | PATCH /todos/1/status | `{"status":"INVALID"}` | Bearer {token} | 400 | code=4000 Invalid |
| 22 | Delete Todo | DELETE /todos/1 | - | Bearer {token} | 200 | code=1000 Delete Successfully |
| 23 | Delete Todo No Own | DELETE /todos/99 | - | Bearer {tokenB} | 404 | code=4007 Todo not found |

### Group 4: Security & Data Isolation

| # | Test Case | Method | Endpoint | Expected | Notes |
|---|-----------|--------|----------|----------|-------|
| 24 | Unauthorized (No Token) | GET | /todos | 401 | code=4005 |
| 25 | User A Not See User B Todo | GET | /todos (userA token) | User A todos only | TodoB không hiển thị |
| 26 | User A Can't Update User B Todo | PUT | /todos/{idB} (userA token) | 404 | code=4007 |
| 27 | User A Can't Delete User B Todo | DELETE | /todos/{idB} (userA token) | 404 | code=4007 |

### Summary Status:
- ✅ **Pass:** 20/27
- ❌ **Fail:** 7/27 (lỗi critical trong phần B)

---

## F. Kết luận & Khuyến cáo

### ❌ Kết luận: **BACKEND CHƯA PASS MVP**

**Vì sao?**
1. **7 lỗi critical** khiến các feature cốt lõi không hoạt động
2. **UpdateToDo endpoint broken** - không update được dữ liệu chính
3. **Login không check enabled** - bảo mật
4. **UserController endpoint sai HTTP method** - API design sai

### 🔧 Cần sửa tối thiểu 7 lỗi critical (Effort: 2-3 giờ)

**Priority 1 (FIX ASAP):**
- [ ] Lỗi #2: Fix updateToDo() - thêm setTitle, setDescription, setStatus, setDueDate
- [ ] Lỗi #1: Fix login - thêm check enabled
- [ ] Lỗi #3: Fix UserController - đổi POST → GET /me
- [ ] Lỗi #4: Fix TodoCreationRequest validation

**Priority 2:**
- [ ] Lỗi #5: Fix TodoUpdateRequest - xóa JPA annotations
- [ ] Lỗi #6: Fix token expiration - 9999 → 3600
- [ ] Lỗi #7: Fix AuthenticationRequest - thêm @NotBlank

### ✅ HAPPY PATH (Sau khi fix):

Nếu fix xong 7 lỗi trên + 5 lỗi medium, backend sẽ:
- ✅ Register/Login/Logout hoạt động đúng
- ✅ Todo CRUD đầy đủ & ownership bảo vệ
- ✅ Validation chặt, exception handling chuẩn
- ✅ Có thể demo cho khách hàng (MVP)

### 🚀 Tiếp theo sau MVP (Optional):

1. **Validation Enhancement:**
   - Validate dueDate không quá khứ
   - Validate password strength
   - Validate email domain (nếu cần)

2. **Business Logic:**
   - Auto-set completedAt khi status=DONE
   - Auto-clear completedAt khi status từ DONE→khác
   - Handling cascade delete vs soft delete

3. **Code Quality:**
   - Unit test: AuthenticationServiceTest, TodoServiceTest, etc.
   - Integration test
   - MapStruct mapper
   - Better error messages

4. **Infrastructure:**
   - Dockerfile + docker-compose.yml
   - CI/CD (GitHub Actions)
   - README chi tiết

5. **Advanced Security:**
   - Refresh token
   - Token blacklist/logout
   - Rate limiting
   - Input sanitization

---

### 📊 Điểm số cuối cùng

```
┌─────────────────────────────────┬────────┐
│ Hạng mục                        │ Điểm   │
├─────────────────────────────────┼────────┤
│ Project Structure               │ 10/10  │
│ Database Design                 │ 9/10   │
│ Authentication/Security         │ 6/10   │ ← Lỗi critical
│ CRUD Operations                 │ 5/10   │ ← UpdateToDo broken
│ Validation                      │ 6/10   │
│ Exception Handling              │ 8/10   │
│ Code Quality/Patterns           │ 7/10   │
├─────────────────────────────────┼────────┤
│ TỔNG CỘNG                       │ 51/70  │ → 73% (FAIR)
└─────────────────────────────────┴────────┘
```

**Status:** 🔴 NOT READY FOR PRODUCTION - Cần fix 7 lỗi critical

---

**Reviewer:** Senior Backend QA Engineer  
**Contact:** Code Review  
**Date:** 22/05/2026  
**Next Review:** Sau khi fix 7 lỗi critical
