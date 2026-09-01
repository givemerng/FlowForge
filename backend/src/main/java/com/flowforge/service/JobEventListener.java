package com.flowforge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.dto.response.JobResponse;
import com.flowforge.entity.Job;
import com.flowforge.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class JobEventListener {
    private static final Logger logger = LoggerFactory.getLogger(JobEventListener.class);

    private final JobRepository jobRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public JobEventListener(JobRepository jobRepository, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${flowforge.rabbitmq.job-events-queue:flowforge.job.events}")
    @Transactional(readOnly = true)
    public void handleJobEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            Long jobId = Long.valueOf(payload.get("jobId").toString());
            
            jobRepository.findById(jobId).ifPresent(job -> {
                JobResponse response = JobResponse.from(job);
                logger.debug("Broadcasting STOMP event for job {} status {}", jobId, job.getStatus());
                messagingTemplate.convertAndSend("/topic/jobs/" + jobId, response);
                messagingTemplate.convertAndSend("/topic/jobs", response); // Broadcast to list as well
            });
        } catch (Exception e) {
            logger.error("Error processing job event message: {}", message, e);
        }
    }
}
