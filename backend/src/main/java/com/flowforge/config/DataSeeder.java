package com.flowforge.config;

import com.flowforge.entity.User;
import com.flowforge.entity.Project;
import com.flowforge.entity.Task;
import com.flowforge.entity.Notification;
import com.flowforge.entity.ProjectMember;
import com.flowforge.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Seeds the database with demo data in development profile.
 * To activate: SPRING_PROFILES_ACTIVE=dev
 * WARNING: Never use these credentials in production.
 */
@Configuration
@Profile("dev")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepo,
            ProjectRepository projectRepo,
            TaskRepository taskRepo,
            ProjectMemberRepository memberRepo,
            NotificationRepository notifRepo,
            PasswordEncoder encoder) {

        return args -> {
            if (userRepo.count() > 0) {
                log.info("Database already seeded, skipping.");
                return;
            }

            log.info("Seeding development data...");

            // ── Users ───────────────────────────────────────────
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@flowforge.dev");
            admin.setPassword(encoder.encode("admin123!"));
            admin.setRole(User.Role.ADMIN);
            userRepo.save(admin);

            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@flowforge.dev");
            manager.setPassword(encoder.encode("manager123!"));
            manager.setRole(User.Role.MANAGER);
            userRepo.save(manager);

            User member1 = new User();
            member1.setUsername("alice");
            member1.setEmail("alice@flowforge.dev");
            member1.setPassword(encoder.encode("alice123!"));
            member1.setRole(User.Role.MEMBER);
            userRepo.save(member1);

            User member2 = new User();
            member2.setUsername("bob");
            member2.setEmail("bob@flowforge.dev");
            member2.setPassword(encoder.encode("bob123!"));
            member2.setRole(User.Role.MEMBER);
            userRepo.save(member2);

            // ── Projects ─────────────────────────────────────────
            Project p1 = new Project();
            p1.setName("FlowForge Platform");
            p1.setDescription("The main FlowForge application development project.");
            p1.setOwner(admin);
            projectRepo.save(p1);

            Project p2 = new Project();
            p2.setName("Mobile App");
            p2.setDescription("Cross-platform mobile companion app.");
            p2.setOwner(manager);
            projectRepo.save(p2);

            // ── Project Members ──────────────────────────────────
            for (User u : new User[]{admin, manager, member1, member2}) {
                ProjectMember pm = new ProjectMember();
                pm.setProject(p1);
                pm.setUser(u);
                pm.setRole(u.getRole());
                memberRepo.save(pm);
            }

            // ── Tasks ────────────────────────────────────────────
            String[][] tasksData = {
                {"Implement JWT Authentication", "Complete real JWT security", "DONE", "HIGH"},
                {"Build Kanban Board", "Drag and drop task management", "DONE", "HIGH"},
                {"Set up CI/CD Pipeline", "GitHub Actions configuration", "IN_PROGRESS", "MEDIUM"},
                {"Write Unit Tests", "JUnit and Pytest coverage", "TODO", "MEDIUM"},
                {"Performance Optimization", "Redis caching and query tuning", "TODO", "LOW"},
                {"Mobile API Design", "REST API for mobile clients", "REVIEW", "HIGH"},
                {"Database Backup Strategy", "Automated backups", "BLOCKED", "CRITICAL"},
            };

            for (String[] t : tasksData) {
                Task task = new Task();
                task.setTitle(t[0]);
                task.setDescription(t[1]);
                task.setStatus(Task.Status.valueOf(t[2]));
                task.setPriority(Task.Priority.valueOf(t[3]));
                task.setProject(p1);
                task.setAssignedTo(member1);
                task.setDeadline(LocalDateTime.now().plusDays(14));
                taskRepo.save(task);
            }

            // ── Notifications ────────────────────────────────────
            Notification n1 = new Notification();
            n1.setUser(member1);
            n1.setTitle("Welcome to FlowForge!");
            n1.setBody("Your account has been set up. Start by creating or joining a project.");
            notifRepo.save(n1);

            Notification n2 = new Notification();
            n2.setUser(admin);
            n2.setTitle("Database Backup task is BLOCKED");
            n2.setBody("Task 'Database Backup Strategy' has been blocked. Action required.");
            notifRepo.save(n2);

            log.info("✅ Dev seed data created successfully.");
            log.info("   Admin: admin / admin123!");
            log.info("   Manager: manager / manager123!");
            log.info("   Member: alice / alice123!");
        };
    }
}
