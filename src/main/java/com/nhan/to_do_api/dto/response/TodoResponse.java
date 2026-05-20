package com.nhan.to_do_api.dto.response;

import com.nhan.to_do_api.entity.Category;
import com.nhan.to_do_api.enums.Priority;
import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TodoResponse {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   Long id;
   String title;
   String description;
   TodoStatus status;
   Priority priority;
   LocalDate dueDate;
   LocalDateTime completedAt;
   LocalDateTime createdAt;
   CategoryResponse category;


}
