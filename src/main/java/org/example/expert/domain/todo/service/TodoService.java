package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);
        String weather = weatherClient.getTodayWeather();

        Todo savedTodo = todoRepository.save(new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        ));

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                createUserResponse(user)
        );
    }

    public Page<TodoResponse> getTodos(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return todoRepository.findAllByOrderByModifiedAtDesc(pageable)
                .map(this::toTodoResponse);
    }

    public TodoResponse getTodo(long todoId) {
        return todoRepository.findById(todoId)
                .map(this::toTodoResponse)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
    }

    private TodoResponse toTodoResponse(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                createUserResponse(todo.getUser()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    private UserResponse createUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}