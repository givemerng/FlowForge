package com.flowforge.controller;

import com.flowforge.dto.response.JobResponse;
import com.flowforge.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job", description = "API endpoints for Job")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Jobs")
    public ResponseEntity<List<JobResponse>> jobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(jobService.getJobs(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Job")
    public ResponseEntity<JobResponse> job(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry")
    public ResponseEntity<JobResponse> retry(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.retryJob(id));
    }
}
