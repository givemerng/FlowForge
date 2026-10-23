package com.flowforge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.dto.request.ReportRequest;
import com.flowforge.dto.response.JobResponse;
import com.flowforge.dto.response.ReportJobResponse;
import com.flowforge.dto.response.ReportResponse;
import com.flowforge.entity.Job;
import com.flowforge.entity.Project;
import com.flowforge.entity.Report;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.JobRepository;
import com.flowforge.repository.ReportRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final JobRepository jobRepository;
    private final ProjectService projectService;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserService currentUserService;
    private final JobQueuePublisher jobQueuePublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ReportService(ReportRepository reportRepository, JobRepository jobRepository,
                         ProjectService projectService,
                         ProjectAccessService projectAccessService,
                         CurrentUserService currentUserService, JobQueuePublisher jobQueuePublisher,
                         SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.jobRepository = jobRepository;
        this.projectService = projectService;
        this.projectAccessService = projectAccessService;
        this.currentUserService = currentUserService;
        this.jobQueuePublisher = jobQueuePublisher;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(int page, int size) {
        User user = currentUserService.requireCurrentUser();
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        List<Report> reports;
        if (user.getRole() == User.Role.ADMIN) {
            reports = reportRepository.findAll(pageRequest).getContent();
        } else {
            reports = reportRepository.findAccessibleByUserId(user.getId(), pageRequest);
        }
        return reports.stream().map(ReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse getReport(Long id) {
        Report report = requireReport(id);
        projectAccessService.requireAccess(report.getProject().getId(), currentUserService.requireCurrentUser());
        return ReportResponse.from(report);
    }

    @Transactional
    public ReportJobResponse generateReport(ReportRequest request) {
        User user = currentUserService.requireCurrentUser();
        projectAccessService.requireAccess(request.projectId(), user);
        Project project = projectService.requireProject(request.projectId());

        String idempotencyKey = UUID.randomUUID().toString();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "type", "REPORT",
                    "reportType", request.type(),
                    "projectId", request.projectId(),
                    "idempotencyKey", idempotencyKey
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create report job payload");
        }

        Job job = new Job();
        job.setType("REPORT");
        job.setIdempotencyKey(idempotencyKey);
        job.setStatus(Job.Status.QUEUED);
        job.setPayload(payload);
        Job savedJob = jobRepository.save(job);

        Report report = new Report();
        report.setProject(project);
        report.setCreatedBy(user);
        report.setJob(savedJob);
        report.setType(request.type());
        Report savedReport = reportRepository.save(report);

        jobQueuePublisher.publishAfterCommit(payload);
        JobResponse jobResponse = JobResponse.from(savedJob);
        messagingTemplate.convertAndSend("/topic/jobs/" + savedJob.getId(), jobResponse);

        return new ReportJobResponse(savedReport.getId(), savedJob.getId(), "QUEUED");
    }

    private Report requireReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }
}
