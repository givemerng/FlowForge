package com.flowforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.dto.request.LoginRequest;
import com.flowforge.dto.request.SignupRequest;
import com.flowforge.entity.User;
import com.flowforge.repository.UserRepository;
import com.flowforge.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
    }

    @Test
    void testRegisterSuccess() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        assertThat(userRepository.findByUsername("testuser")).isPresent();
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        // Create user first
        User user = new User();
        user.setUsername("duplicate");
        user.setEmail("dup@example.com");
        user.setPassword(passwordEncoder.encode("pass"));
        user.setRole(User.Role.MEMBER);
        userRepository.save(user);

        SignupRequest request = new SignupRequest();
        request.setUsername("duplicate");
        request.setEmail("other@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Setup user
        User user = new User();
        user.setUsername("loginuser");
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(User.Role.MEMBER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        User user = new User();
        user.setUsername("loginuser2");
        user.setEmail("login2@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setRole(User.Role.MEMBER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("loginuser2");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }
}
