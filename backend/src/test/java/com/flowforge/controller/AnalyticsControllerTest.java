package com.flowforge.controller;

import com.flowforge.entity.Project;
import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.ProjectRepository;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:analyticstestdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672",
    "spring.data.redis.host=localhost",
    "flowforge.app.jwtSecret=dGVzdFNlY3JldEtleUZvckZsb3dGb3JnZVRlc3RpbmdQdXJwb3Nlc09ubHk=",
    "flowforge.app.jwtExpirationMs=86400000"
})
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private com.flowforge.repository.UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    private com.flowforge.service.CurrentUserService currentUserService;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("TRUNCATE TABLE projects");
        jdbcTemplate.execute("TRUNCATE TABLE tasks");
        jdbcTemplate.execute("TRUNCATE TABLE audit_logs");
        jdbcTemplate.execute("TRUNCATE TABLE project_members");
        jdbcTemplate.execute("TRUNCATE TABLE comments");
        jdbcTemplate.execute("TRUNCATE TABLE reports");
        jdbcTemplate.execute("TRUNCATE TABLE job_attempts");
        jdbcTemplate.execute("TRUNCATE TABLE jobs");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(User.Role.MEMBER);
        userRepository.save(testUser);

        testProject = new Project();
        testProject.setName("Analytics Project");
        testProject.setOwner(testUser);
        projectRepository.save(testProject);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void testGetOverviewReturnsOk() throws Exception {
        when(currentUserService.requireCurrentUser()).thenReturn(testUser);
        task(Task.Status.DONE, Task.Priority.HIGH, null);
        task(Task.Status.IN_PROGRESS, Task.Priority.MEDIUM, LocalDateTime.now().minusDays(1));
        task(Task.Status.BLOCKED, Task.Priority.CRITICAL, LocalDateTime.now().plusDays(1));

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(3))
                .andExpect(jsonPath("$.completedTasks").value(1))
                .andExpect(jsonPath("$.inProgressTasks").value(1))
                .andExpect(jsonPath("$.blockedTasks").value(1))
                .andExpect(jsonPath("$.overdueTasks").value(1))
                .andExpect(jsonPath("$.tasksByStatus.DONE").value(1))
                .andExpect(jsonPath("$.tasksByPriority.CRITICAL").value(1));
    }

    @Test
    void testGetOverviewRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void testGetProjectAnalyticsReturnsOk() throws Exception {
        when(currentUserService.requireCurrentUser()).thenReturn(testUser);
        task(Task.Status.DONE, Task.Priority.HIGH, null);
        mockMvc.perform(get("/api/analytics/project/" + testProject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(1))
                .andExpect(jsonPath("$.completionPercentage").value(100.0));
    }

    private Task task(Task.Status status, Task.Priority priority, LocalDateTime deadline) {
        Task task = new Task();
        task.setTitle(status.name());
        task.setStatus(status);
        task.setPriority(priority);
        task.setDeadline(deadline);
        task.setProject(testProject);
        return taskRepository.save(task);
    }
}
