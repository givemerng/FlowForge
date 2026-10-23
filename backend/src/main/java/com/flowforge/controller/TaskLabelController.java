package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.service.LabelService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/{taskId}/labels")
@Tag(name = "TaskLabel", description = "API endpoints for TaskLabel")
public class TaskLabelController {

    private final LabelService labelService;

    public TaskLabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @PostMapping("/{labelId}")
    @Auditable(action = "ASSIGN_LABEL", resource = "Task")
    @Operation(summary = "Add Label To Task")
    public ResponseEntity<Void> addLabelToTask(@PathVariable Long taskId, @PathVariable Long labelId) {
        labelService.addLabelToTask(taskId, labelId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{labelId}")
    @Auditable(action = "REMOVE_LABEL", resource = "Task")
    @Operation(summary = "Remove Label From Task")
    public ResponseEntity<Void> removeLabelFromTask(@PathVariable Long taskId, @PathVariable Long labelId) {
        labelService.removeLabelFromTask(taskId, labelId);
        return ResponseEntity.noContent().build();
    }
}
