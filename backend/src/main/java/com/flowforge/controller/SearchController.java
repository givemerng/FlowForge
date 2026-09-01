package com.flowforge.controller;

import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public SearchController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        String query = q.toLowerCase().trim();
        if (query.isBlank()) {
            return ResponseEntity.ok(Map.of("tasks", List.of(), "users", List.of()));
        }

        List<Task> tasks = taskRepository.findAll().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(query) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(query)))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> taskResults = tasks.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("status", t.getStatus());
            m.put("priority", t.getPriority());
            m.put("projectId", t.getProject().getId());
            return m;
        }).collect(Collectors.toList());

        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query) ||
                        u.getEmail().toLowerCase().contains(query))
                .limit(5)
                .collect(Collectors.toList());

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
