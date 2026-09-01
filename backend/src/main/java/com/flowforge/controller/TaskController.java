package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.dto.request.TaskAssignRequest;
import com.flowforge.dto.request.TaskStatusRequest;
import com.flowforge.dto.response.TaskResponse;
import com.flowforge.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PutMapping("/{id}/status")
    @Auditable(action = "UPDATE_TASK_STATUS", resource = "Task")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest status) {
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    @PutMapping("/{id}/assign")
    @Auditable(action = "ASSIGN_TASK", resource = "Task")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long id, @Valid @RequestBody TaskAssignRequest request) {
        return ResponseEntity.ok(taskService.assignTask(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Auditable(action = "DELETE_TASK", resource = "Task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
