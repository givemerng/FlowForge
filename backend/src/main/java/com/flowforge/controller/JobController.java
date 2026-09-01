package com.flowforge.controller;

import com.flowforge.dto.response.JobResponse;
import com.flowforge.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> jobs() {
        return ResponseEntity.ok(jobService.getJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> job(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<JobResponse> retry(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.retryJob(id));
    }
}
