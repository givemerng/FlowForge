package com.flowforge.service;

import com.flowforge.dto.response.JobResponse;
import com.flowforge.entity.Job;
import com.flowforge.entity.JobAttempt;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.JobAttemptRepository;
import com.flowforge.repository.JobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {
    private static final int MAX_ATTEMPTS = 3;
    private final JobRepository jobRepository;
    private final JobAttemptRepository jobAttemptRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Value("${flowforge.rabbitmq.jobs-queue:flowforge.jobs}")
    private String jobsQueue;

    public JobService(JobRepository jobRepository, JobAttemptRepository jobAttemptRepository, RabbitTemplate rabbitTemplate, AuditService auditService, CurrentUserService currentUserService) {
        this.jobRepository = jobRepository;
        this.jobAttemptRepository = jobAttemptRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs() {
        return jobRepository.findAll().stream().map(JobResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {
        return JobResponse.from(requireJob(id));
    }

    @Transactional
    public JobResponse retryJob(Long id) {
        Job job = requireJob(id);
        if (job.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Job has reached the retry limit");
        }
        job.setStatus(Job.Status.QUEUED);
        job.setAttemptCount(job.getAttemptCount() + 1);
        Job saved = jobRepository.save(job);

        JobAttempt attempt = new JobAttempt();
        attempt.setJob(saved);
        attempt.setAttemptNumber(saved.getAttemptCount());
        jobAttemptRepository.save(attempt);

        rabbitTemplate.convertAndSend(jobsQueue, saved.getPayload());
        auditService.record(currentUserService.requireCurrentUser(), "JOB_RETRIED", "Job", saved.getId(), null);
        return JobResponse.from(saved);
    }

    private Job requireJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }
}
