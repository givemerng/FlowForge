package com.flowforge.service;

import com.flowforge.dto.response.JobResponse;
import com.flowforge.entity.Job;
import com.flowforge.entity.JobAttempt;
import com.flowforge.entity.User;
import com.flowforge.repository.JobAttemptRepository;
import com.flowforge.repository.JobRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {
    private static final int MAX_ATTEMPTS = 3;
    private final JobRepository jobRepository;
    private final JobAttemptRepository jobAttemptRepository;
    private final JobQueuePublisher jobQueuePublisher;
    private final JobAccessService jobAccessService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public JobService(JobRepository jobRepository, JobAttemptRepository jobAttemptRepository,
                      JobQueuePublisher jobQueuePublisher, JobAccessService jobAccessService,
                      AuditService auditService, CurrentUserService currentUserService,
                      SimpMessagingTemplate messagingTemplate) {
        this.jobRepository = jobRepository;
        this.jobAttemptRepository = jobAttemptRepository;
        this.jobQueuePublisher = jobQueuePublisher;
        this.jobAccessService = jobAccessService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs(int page, int size) {
        User user = currentUserService.requireCurrentUser();
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200));
        List<Job> jobs = user.getRole() == User.Role.ADMIN
                ? jobRepository.findAll(pageRequest).getContent()
                : jobRepository.findAccessibleByUserId(user.getId(), pageRequest);
        return jobs.stream().map(JobResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {
        return JobResponse.from(jobAccessService.requireAccess(id, currentUserService.requireCurrentUser()));
    }

    @Transactional
    public JobResponse retryJob(Long id) {
        User user = currentUserService.requireCurrentUser();
        Job job = jobAccessService.requireManager(id, user);
        if (job.getStatus() != Job.Status.FAILED) {
            throw new IllegalStateException("Only failed jobs can be retried");
        }
        long manualRetryCount = jobAttemptRepository.countByJobId(id);
        if (manualRetryCount >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Job has reached the manual retry limit");
        }
        job.setStatus(Job.Status.QUEUED);
        job.setAttemptCount(0);
        job.setLastError(null);
        Job saved = jobRepository.save(job);

        JobAttempt attempt = new JobAttempt();
        attempt.setJob(saved);
        attempt.setAttemptNumber((int) manualRetryCount + 1);
        jobAttemptRepository.save(attempt);

        jobQueuePublisher.publishAfterCommit(saved.getPayload());
        auditService.record(user, "JOB_RETRIED", "Job", saved.getId(), null);
        
        JobResponse response = JobResponse.from(saved);
        messagingTemplate.convertAndSend("/topic/jobs/" + saved.getId(), response);
        return response;
    }

}
