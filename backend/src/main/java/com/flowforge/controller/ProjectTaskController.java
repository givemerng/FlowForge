package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.dto.request.TaskRequest;
import com.flowforge.dto.response.TaskResponse;
import com.flowforge.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class ProjectTaskController {
    private final TaskService taskService;

    public ProjectTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getProjectTasks(projectId));
    }

    @PostMapping
    @Auditable(action = "CREATE_TASK", resource = "Task")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId, @Valid @RequestBody TaskRequest task) {
        return ResponseEntity.ok(taskService.createTask(projectId, task));
    }
}
