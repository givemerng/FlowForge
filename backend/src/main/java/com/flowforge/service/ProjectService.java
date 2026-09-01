package com.flowforge.service;

import com.flowforge.dto.request.ProjectRequest;
import com.flowforge.dto.response.ProjectResponse;
import com.flowforge.entity.Project;
import com.flowforge.entity.ProjectMember;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.ProjectMemberRepository;
import com.flowforge.repository.ProjectRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, CurrentUserService currentUserService, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    @Cacheable("projects")
    public List<ProjectResponse> getProjects() {
        return projectRepository.findAll().stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "project", key = "#id")
    public ProjectResponse getProject(Long id) {
        return ProjectResponse.from(requireProject(id));
    }

    @Transactional
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public ProjectResponse createProject(ProjectRequest request) {
        User owner = currentUserService.requireCurrentUser();
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        Project saved = projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setProject(saved);
        member.setUser(owner);
        member.setRole(owner.getRole());
        projectMemberRepository.save(member);

        auditService.record(owner, "PROJECT_CREATED", "Project", saved.getId(), null);
        return ProjectResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = requireProject(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);
        auditService.record(currentUserService.requireCurrentUser(), "PROJECT_UPDATED", "Project", saved.getId(), null);
        return ProjectResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = {"projects", "project"}, allEntries = true)
    public void deleteProject(Long id) {
        Project project = requireProject(id);
        projectRepository.delete(project);
        auditService.record(currentUserService.requireCurrentUser(), "PROJECT_DELETED", "Project", id, null);
    }

    public Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    public void checkProjectAccess(Long projectId, User user) {
        Project project = requireProject(projectId);
        // Simple check: if not admin/manager, verify they are a member
        if (user.getRole() == User.Role.MEMBER) {
            projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                    .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied"));
        }
    }
}
