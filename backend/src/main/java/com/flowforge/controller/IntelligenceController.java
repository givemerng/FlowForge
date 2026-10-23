package com.flowforge.controller;

import com.flowforge.dto.response.RiskAnalysisResponse;
import com.flowforge.service.IntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class IntelligenceController {
    private final IntelligenceService intelligenceService;

    public IntelligenceController(IntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping("/tasks/{taskId}/risk")
    public ResponseEntity<RiskAnalysisResponse> analyzeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(intelligenceService.analyzeTask(taskId));
    }
}
