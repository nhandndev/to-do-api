package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoCreationRequest {
    @Size(min = 1, max = 100 , message = "Title mus be 3-100 characters")
    String title;
    String description;
    TodoStatus status;
}
