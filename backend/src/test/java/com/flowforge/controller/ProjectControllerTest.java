package com.flowforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.entity.Project;
import com.flowforge.entity.User;
import com.flowforge.repository.ProjectRepository;
import com.flowforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
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
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private com.flowforge.service.CurrentUserService currentUserService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

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

        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password123");
        adminUser.setRole(User.Role.ADMIN);
        userRepository.save(adminUser);

        when(currentUserService.requireCurrentUser()).thenReturn(adminUser);
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testCreateProject() throws Exception {
        Project project = new Project();
        project.setName("New Project");
        project.setDescription("Project Description");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("Project Description"));

        assertThat(projectRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetProjects() throws Exception {
        when(currentUserService.requireCurrentUser()).thenReturn(testUser);
        Project project = new Project();
        project.setName("Existing Project");
        project.setOwner(testUser);
        projectRepository.save(project);

        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Existing Project"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void testMemberCannotReadUnrelatedProject() throws Exception {
        when(currentUserService.requireCurrentUser()).thenReturn(testUser);
        User otherOwner = new User();
        otherOwner.setUsername("otherowner");
        otherOwner.setEmail("other@example.com");
        otherOwner.setPassword("password123");
        otherOwner.setRole(User.Role.MANAGER);
        userRepository.save(otherOwner);

        Project project = new Project();
        project.setName("Private Project");
        project.setOwner(otherOwner);
        projectRepository.save(project);

        mockMvc.perform(get("/api/projects/" + project.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "othermanager", roles = {"MANAGER"})
    void testManagerCannotUpdateOrDeleteUnrelatedProject() throws Exception {
        User owner = new User();
        owner.setUsername("projectowner");
        owner.setEmail("projectowner@example.com");
        owner.setPassword("password123");
        owner.setRole(User.Role.MANAGER);
        userRepository.save(owner);

        User otherManager = new User();
        otherManager.setUsername("othermanager");
        otherManager.setEmail("othermanager@example.com");
        otherManager.setPassword("password123");
        otherManager.setRole(User.Role.MANAGER);
        userRepository.save(otherManager);
        when(currentUserService.requireCurrentUser()).thenReturn(otherManager);

        Project project = new Project();
        project.setName("Owner Project");
        project.setOwner(owner);
        projectRepository.save(project);

        mockMvc.perform(put("/api/projects/" + project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Blocked update\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/projects/" + project.getId()))
                .andExpect(status().isForbidden());
    }
}
