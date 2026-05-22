package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoCreationRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 4, max = 100 , message = "Title must be 4-100 characters")
    String title;
    String description;
    TodoStatus status;
}
