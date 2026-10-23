package com.flowforge.controller;

import com.flowforge.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for project and task analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get Overview", description = "Retrieves overall project and task analytics overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(analyticsService.overview());
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get Project Analytics", description = "Retrieves analytics for a specific project")
    public ResponseEntity<Map<String, Object>> getProjectAnalytics(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.project(projectId));
    }
}
