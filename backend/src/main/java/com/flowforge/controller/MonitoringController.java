package com.flowforge.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class MonitoringController {
    private final HealthEndpoint healthEndpoint;

    public MonitoringController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthComponent> health() {
        return ResponseEntity.ok(healthEndpoint.health());
    }
}
