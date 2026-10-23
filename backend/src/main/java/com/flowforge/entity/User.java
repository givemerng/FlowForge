package com.flowforge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean taskAssignedNotifications = true;

    @Column(nullable = false)
    private boolean taskCompletedNotifications = true;

    @Column(nullable = false)
    private boolean jobFailedNotifications = true;

    @Column(nullable = false)
    private boolean reportReadyNotifications = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum Role {
        ADMIN, MANAGER, MEMBER
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isTaskAssignedNotifications() { return taskAssignedNotifications; }
    public void setTaskAssignedNotifications(boolean taskAssignedNotifications) { this.taskAssignedNotifications = taskAssignedNotifications; }
    public boolean isTaskCompletedNotifications() { return taskCompletedNotifications; }
    public void setTaskCompletedNotifications(boolean taskCompletedNotifications) { this.taskCompletedNotifications = taskCompletedNotifications; }
    public boolean isJobFailedNotifications() { return jobFailedNotifications; }
    public void setJobFailedNotifications(boolean jobFailedNotifications) { this.jobFailedNotifications = jobFailedNotifications; }
    public boolean isReportReadyNotifications() { return reportReadyNotifications; }
    public void setReportReadyNotifications(boolean reportReadyNotifications) { this.reportReadyNotifications = reportReadyNotifications; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
