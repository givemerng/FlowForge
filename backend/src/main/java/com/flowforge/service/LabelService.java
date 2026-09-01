package com.flowforge.service;

import com.flowforge.dto.request.LabelRequest;
import com.flowforge.dto.response.LabelResponse;
import com.flowforge.entity.Label;
import com.flowforge.entity.Project;
import com.flowforge.entity.Task;
import com.flowforge.entity.TaskLabel;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.LabelRepository;
import com.flowforge.repository.ProjectRepository;
import com.flowforge.repository.TaskLabelRepository;
import com.flowforge.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final CurrentUserService currentUserService;
    private final ProjectService projectService;

    public LabelService(LabelRepository labelRepository, 
                        ProjectRepository projectRepository, 
                        TaskRepository taskRepository, 
                        TaskLabelRepository taskLabelRepository,
                        CurrentUserService currentUserService,
                        ProjectService projectService) {
        this.labelRepository = labelRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskLabelRepository = taskLabelRepository;
        this.currentUserService = currentUserService;
        this.projectService = projectService;
    }

    public List<LabelResponse> getProjectLabels(Long projectId) {
        projectService.checkProjectAccess(projectId, currentUserService.requireCurrentUser());
        return labelRepository.findByProjectId(projectId).stream()
                .map(LabelResponse::from)
                .toList();
    }

    @Transactional
    public LabelResponse createLabel(Long projectId, LabelRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        // Members might be allowed to create labels, but let's say only managers/admins/owners
        // Since no explicit permission framework for labels is given, check access
        projectService.checkProjectAccess(projectId, currentUserService.requireCurrentUser());

        if (labelRepository.existsByProjectIdAndNameIgnoreCase(projectId, request.getName())) {
            throw new IllegalArgumentException("Label with this name already exists in the project");
        }

        Label label = new Label();
        label.setName(request.getName());
        label.setColor(request.getColor());
        label.setProject(project);

        return LabelResponse.from(labelRepository.save(label));
    }

    @Transactional
    public void deleteLabel(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        projectService.checkProjectAccess(label.getProject().getId(), currentUserService.requireCurrentUser());
        labelRepository.delete(label);
    }

    @Transactional
    public void addLabelToTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (!task.getProject().getId().equals(label.getProject().getId())) {
            throw new IllegalArgumentException("Label and Task must belong to the same project");
        }

        projectService.checkProjectAccess(task.getProject().getId(), currentUserService.requireCurrentUser());

        if (taskLabelRepository.findByTaskIdAndLabelId(taskId, labelId).isEmpty()) {
            TaskLabel taskLabel = new TaskLabel();
            taskLabel.setTask(task);
            taskLabel.setLabel(label);
            taskLabelRepository.save(taskLabel);
        }
    }

    @Transactional
    public void removeLabelFromTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        projectService.checkProjectAccess(task.getProject().getId(), currentUserService.requireCurrentUser());

        taskLabelRepository.deleteByTaskIdAndLabelId(taskId, labelId);
    }
}
