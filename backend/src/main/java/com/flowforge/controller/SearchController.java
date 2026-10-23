package com.flowforge.controller;

import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.security.UserDetailsImpl;
import com.flowforge.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "API endpoints for Search")
public class SearchController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public SearchController(TaskRepository taskRepository, UserRepository userRepository, CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @Operation(summary = "Search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        String query = q.toLowerCase().trim();
        if (query.isBlank()) {
            return ResponseEntity.ok(Map.of("tasks", List.of(), "users", List.of()));
        }

        User currentUser = currentUserService.requireCurrentUser();
        List<Task> tasks = currentUser.getRole() == User.Role.ADMIN
                ? taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        query, query, PageRequest.of(0, 10))
                : taskRepository.searchAccessible(currentUser.getId(), query, PageRequest.of(0, 10));

        List<Map<String, Object>> taskResults = tasks.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("status", t.getStatus());
            m.put("priority", t.getPriority());
            m.put("projectId", t.getProject().getId());
            return m;
        }).collect(Collectors.toList());

        List<User> users = currentUser.getRole() == User.Role.MEMBER
                ? List.of()
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query, query, PageRequest.of(0, 5));

        List<Map<String, Object>> userResults = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "tasks", taskResults,
            "users", userResults
        ));
    }
}
