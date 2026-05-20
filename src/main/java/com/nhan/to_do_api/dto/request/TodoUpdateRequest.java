package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.Priority;
import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
public class TodoUpdateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String title;
    String description;
    TodoStatus status;
    Priority priority;
    Date dueDate;
}
