package com.nhan.to_do_api.mapper;

import com.nhan.to_do_api.dto.request.TodoCreationRequest;
import com.nhan.to_do_api.dto.request.TodoUpdateRequest;
import com.nhan.to_do_api.dto.response.TodoResponse;
import com.nhan.to_do_api.entity.Todo;
import com.nhan.to_do_api.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoMapper {
   Todo toTodo(TodoCreationRequest todoCreationRequest);
    TodoResponse toToDoResponse(Todo todo);
    Todo toTodo(TodoUpdateRequest todoUpdateRequest);

}
