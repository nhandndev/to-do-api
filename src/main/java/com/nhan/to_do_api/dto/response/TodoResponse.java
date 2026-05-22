package com.nhan.to_do_api.dto.response;

import com.nhan.to_do_api.enums.TodoStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TodoResponse {
   Long id;
   String title;
   String description;
   TodoStatus status;
   LocalDate dueDate;
   LocalDateTime completedAt;
   LocalDateTime createdAt;
   LocalDateTime updatedAt;
}
