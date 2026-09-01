package com.flowforge.controller;

import com.flowforge.dto.response.UserSummaryResponse;
import com.flowforge.repository.UserRepository;
import com.flowforge.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> me() {
        return ResponseEntity.ok(UserSummaryResponse.from(currentUserService.requireCurrentUser()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserSummaryResponse>> users() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserSummaryResponse::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserSummaryResponse> user(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserSummaryResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
