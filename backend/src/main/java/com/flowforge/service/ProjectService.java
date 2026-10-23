package com.flowforge.service;

import com.flowforge.dto.request.ProjectRequest;
import com.flowforge.dto.response.ProjectResponse;
import com.flowforge.entity.Project;
import com.flowforge.entity.ProjectMember;
import com.flowforge.entity.User;
import com.flowforge.exception.ResourceNotFoundException;
import com.flowforge.repository.ProjectMemberRepository;
import com.flowforge.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
                          ProjectAccessService projectAccessService, CurrentUserService currentUserService,
                          AuditService auditService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectAccessService = projectAccessService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects() {
        User user = currentUserService.requireCurrentUser();
        List<Project> projects = user.getRole() == User.Role.ADMIN
                ? projectRepository.findAll()
                : projectRepository.findAccessibleByUserId(user.getId());
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        User user = currentUserService.requireCurrentUser();
        projectAccessService.requireAccess(id, user);
        return ProjectResponse.from(requireProject(id));
    }

    @Transactional
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
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        projectAccessService.requireManager(id, currentUserService.requireCurrentUser());
        Project project = requireProject(id);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);
        auditService.record(currentUserService.requireCurrentUser(), "PROJECT_UPDATED", "Project", saved.getId(), null);
        return ProjectResponse.from(saved);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectAccessService.requireManager(id, currentUserService.requireCurrentUser());
        Project project = requireProject(id);
        projectRepository.delete(project);
        auditService.record(currentUserService.requireCurrentUser(), "PROJECT_DELETED", "Project", id, null);
    }

    public Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

}
