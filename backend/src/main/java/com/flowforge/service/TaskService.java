package com.flowforge.service;

import com.flowforge.dto.request.TaskAssignRequest;
import com.flowforge.dto.request.TaskRequest;
import com.flowforge.dto.request.TaskStatusRequest;
import com.flowforge.dto.response.TaskResponse;
import com.flowforge.entity.Project;
import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectService projectService, CurrentUserService currentUserService, AuditService auditService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        return TaskResponse.from(requireTask(id));
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        Project project = projectService.requireProject(projectId);
        Task task = new Task();
        applyRequest(task, request);
        task.setProject(project);
        Task saved = taskRepository.save(task);
        auditService.record(currentUserService.requireCurrentUser(), "TASK_CREATED", "Task", saved.getId(), null);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        Task task = requireTask(id);
        task.setStatus(request.getStatus());
        Task saved = taskRepository.save(task);
        auditService.record(currentUserService.requireCurrentUser(), "TASK_STATUS_CHANGED", "Task", saved.getId(), "{\"status\":\"" + request.getStatus().name() + "\"}");
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse assignTask(Long id, TaskAssignRequest request) {
        Task task = requireTask(id);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        task.setAssignedTo(user);
        Task saved = taskRepository.save(task);
        auditService.record(currentUserService.requireCurrentUser(), "TASK_ASSIGNED", "Task", saved.getId(), "{\"assignedTo\":" + user.getId() + "}");
        return TaskResponse.from(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = requireTask(id);
        taskRepository.delete(task);
        auditService.record(currentUserService.requireCurrentUser(), "TASK_DELETED", "Task", id, null);
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        if (request.getAssignedToId() != null) {
            task.setAssignedTo(userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found")));
        }
    }

    private Task requireTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }
}
