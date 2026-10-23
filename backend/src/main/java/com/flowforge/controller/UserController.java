package com.flowforge.controller;

import com.flowforge.aspect.Auditable;
import com.flowforge.dto.request.UserRoleRequest;
import com.flowforge.dto.request.PasswordChangeRequest;
import com.flowforge.dto.request.ProfileRequest;
import com.flowforge.dto.request.UserSettingsRequest;
import com.flowforge.dto.response.UserSummaryResponse;
import com.flowforge.dto.response.UserSettingsResponse;
import com.flowforge.entity.User;
import com.flowforge.repository.UserRepository;
import com.flowforge.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "API endpoints for User")
public class UserController {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    @Operation(summary = "Me")
    public ResponseEntity<UserSummaryResponse> me() {
        return ResponseEntity.ok(UserSummaryResponse.from(currentUserService.requireCurrentUser()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserSummaryResponse> updateMe(@Valid @RequestBody ProfileRequest request) {
        User user = currentUserService.requireCurrentUser();
        userRepository.findByUsername(request.username())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new IllegalStateException("Username is already in use"); });
        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new IllegalStateException("Email is already in use"); });
        user.setUsername(request.username());
        user.setEmail(request.email());
        return ResponseEntity.ok(UserSummaryResponse.from(userRepository.save(user)));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/settings")
    @Operation(summary = "Get current user settings")
    public ResponseEntity<UserSettingsResponse> settings() {
        return ResponseEntity.ok(UserSettingsResponse.from(currentUserService.requireCurrentUser()));
    }

    @PutMapping("/me/settings")
    @Operation(summary = "Update current user settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(@RequestBody UserSettingsRequest request) {
        User user = currentUserService.requireCurrentUser();
        user.setTaskAssignedNotifications(request.taskAssigned());
        user.setTaskCompletedNotifications(request.taskCompleted());
        user.setJobFailedNotifications(request.jobFailed());
        user.setReportReadyNotifications(request.reportReady());
        return ResponseEntity.ok(UserSettingsResponse.from(userRepository.save(user)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Users")
    public ResponseEntity<List<UserSummaryResponse>> users() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserSummaryResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "User")
    public ResponseEntity<UserSummaryResponse> user(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserSummaryResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "UPDATE_USER_ROLE", resource = "User")
    @Operation(summary = "Update Role")
    public ResponseEntity<UserSummaryResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(request.getRole());
                    return userRepository.save(user);
                })
                .map(UserSummaryResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
