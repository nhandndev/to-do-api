package com.nhan.to_do_api.controller;

import com.nhan.to_do_api.dto.request.TodoCreationRequest;
import com.nhan.to_do_api.dto.request.TodoStatusUpdateRequest;
import com.nhan.to_do_api.dto.request.TodoUpdateRequest;
import com.nhan.to_do_api.dto.response.ApiResponse;
import com.nhan.to_do_api.dto.response.TodoResponse;
import com.nhan.to_do_api.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ApiResponse<List<TodoResponse>> getMyToDo (){
        return ApiResponse.<List<TodoResponse>>builder()
                .code(1000)
                .message("Get Successfully")
                .result(todoService.getMyToDos())
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<TodoResponse> getToDo (@PathVariable @Valid Long id){
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Get Successfully")
                .result(todoService.getToDo(id))
                .build();
    }
    @PutMapping("/{id}")
    public ApiResponse<TodoResponse> updateToDo(@PathVariable @Valid Long id, @RequestBody @Valid TodoUpdateRequest todoUpdateRequest){
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Update Successfully")
                .result(todoService.updateToDo(id, todoUpdateRequest))
                .build();
    }
    @PatchMapping("/{id}/status")
    public ApiResponse<TodoResponse> StatusUpdate (@PathVariable @Valid Long id , @RequestBody @Valid TodoStatusUpdateRequest todoStatusUpdateRequest){
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Toggle Complete")
                .result(todoService.updateStatus(id,todoStatusUpdateRequest))
                .build();
    }
    @DeleteMapping("/{id}")
    public ApiResponse<TodoResponse> deleteToDo(@PathVariable @Valid Long id){
        todoService.deleteToDo(id);
        return ApiResponse.<TodoResponse>builder()
                .code(1000)
                .message("Delete Successfully")
                .build();
    }
}
