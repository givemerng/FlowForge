package com.flowforge.controller;

import com.flowforge.entity.Task;
import com.flowforge.repository.TaskRepository;
import com.flowforge.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:analyticstestdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
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

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void testGetOverviewReturnsOk() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOverviewRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"MEMBER"})
    void testGetProjectAnalyticsReturnsOk() throws Exception {
        mockMvc.perform(get("/api/analytics/project/999"))
                .andExpect(status().isOk());
    }
}
