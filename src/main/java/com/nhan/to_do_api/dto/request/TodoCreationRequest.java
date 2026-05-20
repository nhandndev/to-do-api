package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.Priority;
import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class TodoCreationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long categoryid;
    String title;
    String description;
    TodoStatus status;
}
