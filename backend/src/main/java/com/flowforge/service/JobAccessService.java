package com.flowforge.service;

import com.flowforge.entity.Job;
import com.flowforge.entity.Report;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.JobRepository;
import com.flowforge.repository.ReportRepository;
import com.flowforge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobAccessService {
    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;

    public JobAccessService(JobRepository jobRepository, ReportRepository reportRepository,
                            UserRepository userRepository, ProjectAccessService projectAccessService) {
        this.jobRepository = jobRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public Job requireAccess(Long jobId, User user) {
        Job job = requireJob(jobId);
        if (user.getRole() != User.Role.ADMIN) {
            Report report = requireReport(jobId);
            projectAccessService.requireAccess(report.getProject().getId(), user);
        }
        return job;
    }

    @Transactional(readOnly = true)
    public Job requireAccess(Long jobId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return requireAccess(jobId, user);
    }

    @Transactional(readOnly = true)
    public Job requireManager(Long jobId, User user) {
        Job job = requireJob(jobId);
        if (user.getRole() != User.Role.ADMIN) {
            Report report = requireReport(jobId);
            projectAccessService.requireManager(report.getProject().getId(), user);
        }
        return job;
    }

    private Job requireJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Report requireReport(Long jobId) {
        return reportRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Report job not found"));
    }
}
