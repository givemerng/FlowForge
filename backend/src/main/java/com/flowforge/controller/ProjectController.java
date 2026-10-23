package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.dto.request.ProjectRequest;
import com.flowforge.dto.response.ProjectResponse;
import com.flowforge.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project", description = "API endpoints for Project")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Get All Projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getProjects());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Auditable(action = "CREATE_PROJECT", resource = "Project")
    @Operation(summary = "Create Project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest project) {
        return ResponseEntity.ok(projectService.createProject(project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Project By Id")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Auditable(action = "UPDATE_PROJECT", resource = "Project")
    @Operation(summary = "Update Project")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest projectDetails) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Auditable(action = "DELETE_PROJECT", resource = "Project")
    @Operation(summary = "Delete Project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
