package com.flowforge.controller;

import com.flowforge.entity.Task;
import com.flowforge.repository.TaskRepository;
import com.flowforge.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final TaskRepository taskRepository;

    public AnalyticsController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        List<Task> allTasks = taskRepository.findAll();
        long total = allTasks.size();
        long done = allTasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long inProgress = allTasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long blocked = allTasks.stream().filter(t -> t.getStatus() == Task.Status.BLOCKED).count();
        long todo = allTasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();
        long review = allTasks.stream().filter(t -> t.getStatus() == Task.Status.REVIEW).count();
        long overdue = allTasks.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now()) && t.getStatus() != Task.Status.DONE)
                .count();

        double completionPct = total > 0 ? (double) done / total * 100 : 0;

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("TODO", todo);
        byStatus.put("IN_PROGRESS", inProgress);
        byStatus.put("REVIEW", review);
        byStatus.put("BLOCKED", blocked);
        byStatus.put("DONE", done);

        Map<String, Long> byPriority = allTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTasks", total);
        result.put("completedTasks", done);
        result.put("inProgressTasks", inProgress);
        result.put("blockedTasks", blocked);
        result.put("overdueTasks", overdue);
        result.put("completionPercentage", Math.round(completionPct * 10.0) / 10.0);
        result.put("tasksByStatus", byStatus);
        result.put("tasksByPriority", byPriority);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectAnalytics(@PathVariable Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        long total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long overdue = tasks.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now()) && t.getStatus() != Task.Status.DONE)
                .count();

        Map<String, Long> byStatus = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
        Map<String, Long> byPriority = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("totalTasks", total);
        result.put("completedTasks", done);
        result.put("completionPercentage", total > 0 ? Math.round((double) done / total * 1000) / 10.0 : 0);
        result.put("overdueTasks", overdue);
        result.put("tasksByStatus", byStatus);
        result.put("tasksByPriority", byPriority);

        return ResponseEntity.ok(result);
    }
}
