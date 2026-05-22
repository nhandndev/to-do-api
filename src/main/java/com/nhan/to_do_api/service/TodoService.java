package com.nhan.to_do_api.service;

import com.nhan.to_do_api.dto.request.TodoCreationRequest;
import com.nhan.to_do_api.dto.request.TodoStatusUpdateRequest;
import com.nhan.to_do_api.dto.request.TodoUpdateRequest;
import com.nhan.to_do_api.dto.response.TodoResponse;
import com.nhan.to_do_api.entity.Todo;
import com.nhan.to_do_api.entity.User;
import com.nhan.to_do_api.enums.TodoStatus;
import com.nhan.to_do_api.exception.AppException;
import com.nhan.to_do_api.exception.ErrorCode;
import com.nhan.to_do_api.mapper.TodoMapper;
import com.nhan.to_do_api.repository.TodoRepository;
import com.nhan.to_do_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoService {
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TodoMapper todoMapper;
    public TodoResponse createToDo (TodoCreationRequest todoCreationRequest) {
        User currentUser = getCurrentUser();
        Todo todo = Todo.builder()
                .title(todoCreationRequest.getTitle())
                .description(todoCreationRequest.getDescription())
                .status(TodoStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(currentUser)
                .build();
        todo = todoRepository.save(todo);
        return todoMapper.toToDoResponse(todo);
    }
    public  User getCurrentUser() {
        String authentication = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(authentication).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user;
    }
    public List<TodoResponse> getMyToDos() {
        User currentUser = getCurrentUser();
        return todoRepository.findAllByUser(currentUser)
                .stream()
                .map(Todo -> todoMapper.toToDoResponse(Todo))
                .toList();
    }
    public TodoResponse getToDo(Long id) {
        User currentUser = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUser(id,currentUser).orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND));
        return todoMapper.toToDoResponse(todo);
    }
    public TodoResponse updateToDo(Long id, TodoUpdateRequest todoUpdateRequest) {
        User currentUser = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUser(id,currentUser).orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND));
        todo.setUpdatedAt(LocalDateTime.now());
        return todoMapper.toToDoResponse(todoRepository.save(todo));
    }
    public TodoResponse StatusUpdate(Long id, TodoStatusUpdateRequest todoStatusUpdateRequest) {
        User currentUser = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUser(id,currentUser).orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND));
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setStatus(todoStatusUpdateRequest.getStatus());
        return todoMapper.toToDoResponse(todoRepository.save(todo));
    }
    public void deleteToDo(Long id) {
        User currentUser = getCurrentUser();
        Todo todo = todoRepository.findByIdAndUser(id,currentUser).orElseThrow(() -> new AppException(ErrorCode.TODO_NOT_FOUND));
        todoRepository.delete(todo);
    }
}
