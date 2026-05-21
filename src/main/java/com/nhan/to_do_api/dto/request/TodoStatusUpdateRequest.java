package com.nhan.to_do_api.dto.request;

import com.nhan.to_do_api.enums.TodoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoStatusUpdateRequest {
    @NotNull
    TodoStatus status;
}
