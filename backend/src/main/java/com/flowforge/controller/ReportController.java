package com.flowforge.controller;

import com.flowforge.dto.request.ReportRequest;
import com.flowforge.dto.response.ReportJobResponse;
import com.flowforge.dto.response.ReportResponse;
import com.flowforge.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report", description = "API endpoints for Report")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "Get Reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(reportService.getReports(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Report")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReport(id));
    }

    @PostMapping
    @Operation(summary = "Generate Report")
    public ResponseEntity<ReportJobResponse> generateReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.generateReport(request));
    }
}
