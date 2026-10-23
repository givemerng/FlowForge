package com.flowforge.service;

import com.flowforge.entity.Project;
import com.flowforge.entity.ProjectMember;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.ProjectMemberRepository;
import com.flowforge.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAccessService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectAccessService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public void requireAccess(Long projectId, User user) {
        Project project = requireProject(projectId);
        if (user.getRole() == User.Role.ADMIN || isOwner(project, user)) {
            return;
        }
        projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied"));
    }

    @Transactional(readOnly = true)
    public void requireManager(Long projectId, User user) {
        Project project = requireProject(projectId);
        if (user.getRole() == User.Role.ADMIN || isOwner(project, user)) {
            return;
        }
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied"));
        if (membership.getRole() != User.Role.MANAGER) {
            throw new AccessDeniedException("Project manager access required");
        }
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private boolean isOwner(Project project, User user) {
        return project.getOwner() != null && project.getOwner().getId().equals(user.getId());
    }
}
