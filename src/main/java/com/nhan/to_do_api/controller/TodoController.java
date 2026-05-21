package com.nhan.to_do_api.controller;

import com.nhan.to_do_api.dto.request.TodoCreationRequest;
import com.nhan.to_do_api.dto.response.ApiResponse;
import com.nhan.to_do_api.dto.response.TodoResponse;
import com.nhan.to_do_api.entity.Todo;
import com.nhan.to_do_api.entity.User;
import com.nhan.to_do_api.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {
    @Autowired
    private TodoService todoService;
    @PostMapping
    public ApiResponse<TodoResponse> createToDo (@RequestBody @Valid TodoCreationRequest todoCreationRequest ){
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Create Successfully")
                .result(todoService.createToDo(todoCreationRequest))
                .build();
    }
    @GetMapping
    public ApiResponse<List<TodoResponse>> getToDo (){
        return ApiResponse.<List<TodoResponse>>builder()
                .code(1000)
                .message("Get Successfully")
                .result(todoService.getMyToDos())
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> getToDoById (@RequestParam @Valid Long id){
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Get Successfully")
                .result(todoService.getToDoById(id))
                .build();
    }
}
