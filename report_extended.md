# Báo cáo nghiệm thu Backend Todo API (Tiếp tục)

## B. Lỗi nghiêm trọng cần sửa ngay (tiếp)

| Mức độ | Vấn đề | File/Class nghi ngờ | Vì sao sai | Cách sửa |
|--------|--------|--------------------|------------|----------|
| Critical | Thiếu kiểm tra quyền sở hữu khi thao tác Todo/Category | TodoService, CategoryService | User A có thể thao tác dữ liệu User B nếu chỉ truyền id mà không check user | Trong service, luôn lấy `User currentUser = getCurrentUser();` và dùng repository method `findByIdAndUser`/`findAllByUser` để giới hạn. |
| Critical | Chưa kiểm tra `enabled` khi login | AuthenticationService | Người dùng bị khóa vẫn có thể đăng nhập | Thêm kiểm tra `if (!user.isEnabled()) throw new AppException(ErrorCode.USER_NOT_ENABLED);` trong quá trình xác thực. |
| Critical | Chưa kiểm tra category còn todo khi xóa | CategoryService | Xóa category còn todo sẽ gây orphan hoặc lỗi nghiệp vụ | Trước khi xóa, kiểm tra `if (!todoRepository.findAllByCategory(category).isEmpty()) throw new AppException(ErrorCode.CANNOT_DELETE_CATEGORY_HAS_TODOS);` |
| Critical | Duplicate category name per user | CategoryService, CategoryRepository | Cho phép tạo 2 category trùng tên cho cùng user | Thêm method `existsByNameAndUser(String name, User user)` và validate trước khi tạo/sửa. |
| Critical | Enum mapping không sử dụng `EnumType.STRING` | Entity (Role, TodoStatus) | Khi enum thay đổi thứ tự sẽ gây dữ liệu lệch | Đánh dấu `@Enumerated(EnumType.STRING)` cho các enum fields. |
| High | Token hết hạn hoặc không hợp lệ không trả về 401 thích hợp | JwtAuthenticationFilter, SecurityConfig | Người dùng vẫn nhận 200 hoặc lỗi không rõ | Bắt `JwtException`/`ExpiredJwtException` và trả `ErrorCode.UNAUTHORIZED` (4005) với HTTP 401. |
| High | Validation error trả dạng generic, thiếu chi tiết field | GlobalExceptionHandler | Frontend không biết lỗi cụ thể | Khi bắt `MethodArgumentNotValidException`, lấy `exception.getBindingResult().getFieldErrors()` và đưa vào `result` của `ApiResponse`. |
| High | `dueDate` không được validate không cho ngày quá khứ | TodoCreationRequest, TodoUpdateRequest | Người dùng có thể tạo todo với ngày đã qua | Thêm annotation `@FutureOrPresent` trên trường `dueDate`. |
| High | Khi cập nhật status `DONE` không set `completedAt` | TodoService.StatusUpdate | Thông tin hoàn thành không được ghi | Nếu status == DONE → `todo.setCompletedAt(LocalDateTime.now())`; nếu chuyển sang khác → `todo.setCompletedAt(null)`. |
| High | Phương thức đặt tên không tuân camelCase (StatusUpdate) | TodoService, TodoController | Vi phạm chuẩn Java, gây confusion | Đổi thành `statusUpdate` (service) và `updateStatus` (controller). |

## C. Các cải tiến / đề xuất

- **Refactor Entity Lombok**: Loại bỏ `@Data` khỏi `User` và `Todo` để tránh vòng lặp `toString()`/`hashCode()` và rò rỉ password. Dùng `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`.
- **User Enumeration mitigation**: Khi login, trả lỗi chung `INVALID_USERNAME_OR_PASSWORD` cho cả trường hợp không tồn tại và mật khẩu sai.
- **Role trong JWT**: Bổ sung claim `scope` hoặc `roles` khi tạo token để Spring Security có thể dựa vào.
- **Pagination & Filtering**: Thêm `Pageable` vào `TodoRepository.findAllByUser` và expose các tham số `page`, `size`, `sort` trong API list.
- **Audit Log**: Ghi log chi tiết các hành động CRUD quan trọng (tạo, sửa, xóa) cùng userId và timestamp.
- **Soft Delete**: Thay vì xóa vĩnh viễn, đánh dấu `deleted` để giữ lịch sử và tránh orphan.
- **Integration Test Suite**: Viết test cho các trường hợp: 
  - User không thể truy cập tài nguyên của người khác.
  - Token hết hạn / không hợp lệ.
  - Category duplicate, category deletion with todos.
  - Validation lỗi trả về chi tiết.

## D. Checklist nghiệm thu (Cập nhật)

| Hạng mục | Pass/Fail | Ghi chú |
|----------|-----------|---------|
| Package structure | Pass | Đúng chuẩn layer |
| Entity relationships | Pass | `@ManyToOne` / `@OneToMany` đúng, nhưng cần `@Enumerated(EnumType.STRING)` |
| Password encoding | Pass | BCrypt |
| JWT authentication | Pass | Nhưng cần xử lý token lỗi/expiry |
| Ownership checks | Fail | Cần thêm kiểm tra trong service (đã liệt kê) |
| Validation detail | Fail | Cần trả field errors chi tiết |
| Enabled check on login | Fail | Thiếu kiểm tra `enabled` |
| Category unique per user | Fail | Thiếu validation |
| Enum mapping | Fail | Thiếu `@Enumerated(EnumType.STRING)` |
| GlobalExceptionHandler | Pass | Cần cải thiện chi tiết validation |
| Test coverage | Fail | Không có test cho security/ownership |

---

## E. Kế hoạch hành động (3 ngày tới)

1. **Ngày 1**: Sửa `User` và `Todo` entity Lombok, thêm `@Enumerated(EnumType.STRING)`, cập nhật migration nếu cần.
2. **Ngày 1‑2**: Cập nhật `AuthenticationService` để kiểm tra `enabled` và gộp lỗi login.
3. **Ngày 2**: Thêm validation `@FutureOrPresent` cho `dueDate` và kiểm tra duplicate category name.
4. **Ngày 2‑3**: Cập nhật `TodoService`/`CategoryService` thêm ownership checks và `completedAt` logic.
5. **Ngày 3**: Cập nhật `JwtAuthenticationFilter` để bắt `JwtException` và trả 401, cải thiện `GlobalExceptionHandler` trả field errors.
6. **Ngày 3‑4**: Viết unit/integration test cho các case trên.

---

## F. Kết luận

Dự án đã đạt được nền tảng vững chắc, nhưng để đạt chuẩn MVP cần **khắc phục các lỗi nghiêm trọng** (ownership, enabled, enum mapping, token handling) và **cải thiện validation & test coverage**. Sau khi thực hiện các bước trên, dự án sẽ sẵn sàng cho nghiệm thu và triển khai production.
