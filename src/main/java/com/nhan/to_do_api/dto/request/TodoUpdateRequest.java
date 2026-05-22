package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class TodoUpdateRequest {
    @NotBlank
    String title;
    String description;
    TodoStatus status;
    Date dueDate;
}
