package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.dto.request.LabelRequest;
import com.flowforge.dto.response.LabelResponse;
import com.flowforge.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> getProjectLabels(@PathVariable Long projectId) {
        return ResponseEntity.ok(labelService.getProjectLabels(projectId));
    }

    @PostMapping
    @Auditable(action = "CREATE_LABEL", resource = "Label")
    public ResponseEntity<LabelResponse> createLabel(@PathVariable Long projectId, @Valid @RequestBody LabelRequest request) {
        return ResponseEntity.ok(labelService.createLabel(projectId, request));
    }

    @DeleteMapping("/{labelId}")
    @Auditable(action = "DELETE_LABEL", resource = "Label")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long projectId, @PathVariable Long labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.noContent().build();
    }
}
