package com.flowforge.service;

import com.flowforge.dto.response.RiskAnalysisResponse;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
public class IntelligenceService {
    private final TaskRepository taskRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserService currentUserService;
    private final RestClient restClient;

    public IntelligenceService(TaskRepository taskRepository, ProjectAccessService projectAccessService,
                               CurrentUserService currentUserService,
                               @Value("${flowforge.intelligence.base-url:http://localhost:8000}") String baseUrl) {
        this.taskRepository = taskRepository;
        this.projectAccessService = projectAccessService;
        this.currentUserService = currentUserService;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public RiskAnalysisResponse analyzeTask(Long taskId) {
        Long projectId = taskRepository.findProjectIdByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        projectAccessService.requireAccess(projectId, currentUserService.requireCurrentUser());

        try {
            RiskAnalysisResponse response = restClient.get()
                    .uri("/api/risk/{taskId}", taskId)
                    .retrieve()
                    .body(RiskAnalysisResponse.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Intelligence service returned no data");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Intelligence service is unavailable", exception);
        }
    }
}
