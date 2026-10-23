package com.flowforge.controller;

import com.flowforge.entity.Comment;
import com.flowforge.entity.Label;
import com.flowforge.entity.Job;
import com.flowforge.entity.Project;
import com.flowforge.entity.Report;
import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.CommentRepository;
import com.flowforge.repository.LabelRepository;
import com.flowforge.repository.JobRepository;
import com.flowforge.repository.ProjectRepository;
import com.flowforge.repository.ReportRepository;
import com.flowforge.repository.TaskRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.service.CurrentUserService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:authorizationdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "flowforge.app.jwtSecret=dGVzdFNlY3JldEtleUZvckZsb3dGb3JnZVRlc3RpbmdQdXJwb3Nlc09ubHk=",
        "flowforge.app.jwtExpirationMs=86400000"
})
class AuthorizationBoundaryTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean
    private CurrentUserService currentUserService;

    private User outsider;
    private User managerOutsider;
    private Project privateProject;
    private Task privateTask;
    private Label privateLabel;
    private Report privateReport;
    private Job privateJob;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : new String[]{"task_labels", "labels", "comments", "reports", "job_attempts", "jobs", "notifications", "tasks", "project_members", "projects", "audit_logs", "users"}) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        User owner = user("owner", "owner@example.com", User.Role.MANAGER);
        outsider = user("outsider", "outsider@example.com", User.Role.MEMBER);
        managerOutsider = user("manager-outsider", "manager-outsider@example.com", User.Role.MANAGER);
        privateProject = new Project();
        privateProject.setName("Private Project");
        privateProject.setOwner(owner);
        projectRepository.save(privateProject);

        privateTask = new Task();
        privateTask.setTitle("Private Task");
        privateTask.setProject(privateProject);
        taskRepository.save(privateTask);

        privateLabel = new Label();
        privateLabel.setName("Private");
        privateLabel.setColor("#000000");
        privateLabel.setProject(privateProject);
        labelRepository.save(privateLabel);

        Comment comment = new Comment();
        comment.setBody("Private comment");
        comment.setTask(privateTask);
        comment.setAuthor(owner);
        commentRepository.save(comment);

        privateJob = new Job();
        privateJob.setType("REPORT");
        privateJob.setPayload("{\"type\":\"REPORT\"}");
        privateJob.setStatus(Job.Status.FAILED);
        privateJob.setIdempotencyKey("private-job");
        jobRepository.save(privateJob);

        privateReport = new Report();
        privateReport.setProject(privateProject);
        privateReport.setCreatedBy(owner);
        privateReport.setJob(privateJob);
        privateReport.setType("SUMMARY");
        reportRepository.save(privateReport);

        when(currentUserService.requireCurrentUser()).thenReturn(outsider);
    }

    @Test
    @WithMockUser(username = "outsider", roles = "MEMBER")
    void outsiderCannotReadOrMutateLabels() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/labels", privateProject.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/projects/{projectId}/labels", privateProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Blocked\",\"color\":\"#ffffff\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/projects/{projectId}/labels/{labelId}", privateProject.getId(), privateLabel.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider", roles = "MEMBER")
    void outsiderCannotReadOrCreateComments() throws Exception {
        mockMvc.perform(get("/api/tasks/{taskId}/comments", privateTask.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tasks/{taskId}/comments", privateTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Blocked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider", roles = "MEMBER")
    void outsiderCannotReadReportByIdAndCannotSeeItInList() throws Exception {
        mockMvc.perform(get("/api/reports/{id}", privateReport.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "outsider", roles = "MEMBER")
    void outsiderCannotReadTaskRiskAnalysis() throws Exception {
        mockMvc.perform(get("/api/insights/tasks/{taskId}/risk", privateTask.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager-outsider", roles = "MANAGER")
    void unrelatedManagerCannotReadOrRetryPrivateJob() throws Exception {
        when(currentUserService.requireCurrentUser()).thenReturn(managerOutsider);

        mockMvc.perform(get("/api/jobs/{id}", privateJob.getId()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(post("/api/jobs/{id}/retry", privateJob.getId()))
                .andExpect(status().isForbidden());
    }

    private User user(String username, String email, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(role);
        return userRepository.save(user);
    }
}
