package com.flowforge.service;

import com.flowforge.entity.Task;
import com.flowforge.entity.User;
import com.flowforge.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessService projectAccessService;

    public AnalyticsService(TaskRepository taskRepository, CurrentUserService currentUserService,
                            ProjectAccessService projectAccessService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.projectAccessService = projectAccessService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> overview() {
        User user = currentUserService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        Object[] summary = first(user.getRole() == User.Role.ADMIN
                ? taskRepository.summarizeAll(now)
                : taskRepository.summarizeAccessible(user.getId(), now));
        List<Object[]> statusCounts = user.getRole() == User.Role.ADMIN
                ? taskRepository.countAllByStatus()
                : taskRepository.countAccessibleByStatus(user.getId());
        List<Object[]> priorityCounts = user.getRole() == User.Role.ADMIN
                ? taskRepository.countAllByPriority()
                : taskRepository.countAccessibleByPriority(user.getId());
        return response(null, summary, statusCounts, priorityCounts);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> project(Long projectId) {
        projectAccessService.requireAccess(projectId, currentUserService.requireCurrentUser());
        return response(
                projectId,
                first(taskRepository.summarizeProject(projectId, LocalDateTime.now())),
                taskRepository.countProjectByStatus(projectId),
                taskRepository.countProjectByPriority(projectId)
        );
    }

    private Map<String, Object> response(Long projectId, Object[] summary,
                                         List<Object[]> statusCounts, List<Object[]> priorityCounts) {
        long total = number(summary[0]);
        long completed = number(summary[1]);
        Map<String, Long> byStatus = enumCounts(Task.Status.values(), statusCounts);
        Map<String, Long> byPriority = enumCounts(Task.Priority.values(), priorityCounts);

        Map<String, Object> result = new LinkedHashMap<>();
        if (projectId != null) {
            result.put("projectId", projectId);
        }
        result.put("totalTasks", total);
        result.put("completedTasks", completed);
        if (projectId == null) {
            result.put("inProgressTasks", number(summary[2]));
            result.put("blockedTasks", number(summary[3]));
        }
        result.put("overdueTasks", number(summary[4]));
        result.put("completionPercentage", total == 0 ? 0.0 : Math.round((double) completed / total * 1000) / 10.0);
        result.put("tasksByStatus", byStatus);
        result.put("tasksByPriority", byPriority);
        return result;
    }

    private <E extends Enum<E>> Map<String, Long> enumCounts(E[] values, List<Object[]> counts) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (E value : values) {
            result.put(value.name(), 0L);
        }
        for (Object[] count : counts) {
            result.put(((Enum<?>) count[0]).name(), number(count[1]));
        }
        return result;
    }

    private long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private Object[] first(List<Object[]> rows) {
        return rows.isEmpty() ? new Object[]{0L, 0L, 0L, 0L, 0L} : rows.get(0);
    }
}
