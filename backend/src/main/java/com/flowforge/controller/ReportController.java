package com.flowforge.controller;

import com.flowforge.entity.Job;
import com.flowforge.entity.Project;
import com.flowforge.entity.Report;
import com.flowforge.entity.User;
import com.flowforge.repository.JobRepository;
import com.flowforge.repository.ProjectRepository;
import com.flowforge.repository.ReportRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.security.UserDetailsImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${flowforge.rabbitmq.jobs-queue:flowforge.jobs}")
    private String jobsQueue;

    public ReportController(ReportRepository reportRepository, ProjectRepository projectRepository,
                             JobRepository jobRepository, UserRepository userRepository,
                             RabbitTemplate rabbitTemplate, SimpMessagingTemplate messagingTemplate) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public ResponseEntity<List<Report>> getReports(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(reportRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        return reportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetailsImpl principal) {
        Long projectId = Long.valueOf(body.get("projectId").toString());
        String type = body.getOrDefault("type", "PROJECT_SUMMARY").toString();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.badRequest().body("Project not found");

        User user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        // Create Job
        String idempotencyKey = UUID.randomUUID().toString();
        Job job = new Job();
        job.setType("REPORT");
        job.setIdempotencyKey(idempotencyKey);
        job.setStatus(Job.Status.QUEUED);
        String payload = String.format("{\"type\":\"%s\",\"projectId\":%d,\"idempotencyKey\":\"%s\"}", type, projectId, idempotencyKey);
        job.setPayload(payload);
        Job savedJob = jobRepository.save(job);

        // Create Report record
        Report report = new Report();
        report.setProject(project);
        report.setCreatedBy(user);
        report.setJob(savedJob);
        report.setType(type);
        Report savedReport = reportRepository.save(report);

        // Publish to RabbitMQ
        rabbitTemplate.convertAndSend(jobsQueue, payload);

        // Broadcast new job creation
        com.flowforge.dto.response.JobResponse response = com.flowforge.dto.response.JobResponse.from(savedJob);
        messagingTemplate.convertAndSend("/topic/jobs", response);
        messagingTemplate.convertAndSend("/topic/jobs/" + savedJob.getId(), response);

        return ResponseEntity.ok(Map.of(
            "reportId", savedReport.getId(),
            "jobId", savedJob.getId(),
            "status", "QUEUED"
        ));
    }
}
