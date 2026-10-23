package com.flowforge.repository;

import com.flowforge.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignedToId(Long assignedToId);

    @EntityGraph(attributePaths = {"project", "assignedTo", "taskLabels", "taskLabels.label"})
    @Query("select distinct t from Task t where t.project.id = :projectId")
    List<Task> findDetailedByProjectId(@Param("projectId") Long projectId);

    @EntityGraph(attributePaths = {"project", "assignedTo", "taskLabels", "taskLabels.label"})
    @Query("select distinct t from Task t where t.assignedTo.id = :assignedToId")
    List<Task> findDetailedByAssignedToId(@Param("assignedToId") Long assignedToId);

    @EntityGraph(attributePaths = {"project", "assignedTo", "taskLabels", "taskLabels.label"})
    @Query("select t from Task t where t.id = :id")
    Optional<Task> findDetailedById(@Param("id") Long id);

    @Query("select t.project.id from Task t where t.id = :taskId")
    Optional<Long> findProjectIdByTaskId(@Param("taskId") Long taskId);

    @Query("select distinct t from Task t left join ProjectMember pm on pm.project = t.project " +
            "where t.project.owner.id = :userId or pm.user.id = :userId")
    List<Task> findAccessibleByUserId(@Param("userId") Long userId);

    List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);

    @Query("select distinct t from Task t left join ProjectMember pm on pm.project = t.project " +
            "where (t.project.owner.id = :userId or pm.user.id = :userId) " +
            "and (lower(t.title) like lower(concat('%', :query, '%')) " +
            "or lower(coalesce(t.description, '')) like lower(concat('%', :query, '%')))")
    List<Task> searchAccessible(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);

    @Query("select count(t), " +
            "coalesce(sum(case when t.status = DONE then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = IN_PROGRESS then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = BLOCKED then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.deadline < :now and t.status <> DONE then 1 else 0 end), 0) " +
            "from Task t")
    List<Object[]> summarizeAll(@Param("now") LocalDateTime now);

    @Query("select count(t), " +
            "coalesce(sum(case when t.status = DONE then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = IN_PROGRESS then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = BLOCKED then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.deadline < :now and t.status <> DONE then 1 else 0 end), 0) " +
            "from Task t where t.project.owner.id = :userId or exists (" +
            "select pm.id from ProjectMember pm where pm.project = t.project and pm.user.id = :userId)")
    List<Object[]> summarizeAccessible(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("select count(t), " +
            "coalesce(sum(case when t.status = DONE then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = IN_PROGRESS then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.status = BLOCKED then 1 else 0 end), 0), " +
            "coalesce(sum(case when t.deadline < :now and t.status <> DONE then 1 else 0 end), 0) " +
            "from Task t where t.project.id = :projectId")
    List<Object[]> summarizeProject(@Param("projectId") Long projectId, @Param("now") LocalDateTime now);

    @Query("select t.status, count(t) from Task t group by t.status")
    List<Object[]> countAllByStatus();

    @Query("select t.status, count(t) from Task t where t.project.owner.id = :userId or exists (" +
            "select pm.id from ProjectMember pm where pm.project = t.project and pm.user.id = :userId) " +
            "group by t.status")
    List<Object[]> countAccessibleByStatus(@Param("userId") Long userId);

    @Query("select t.status, count(t) from Task t where t.project.id = :projectId group by t.status")
    List<Object[]> countProjectByStatus(@Param("projectId") Long projectId);

    @Query("select t.priority, count(t) from Task t group by t.priority")
    List<Object[]> countAllByPriority();

    @Query("select t.priority, count(t) from Task t where t.project.owner.id = :userId or exists (" +
            "select pm.id from ProjectMember pm where pm.project = t.project and pm.user.id = :userId) " +
            "group by t.priority")
    List<Object[]> countAccessibleByPriority(@Param("userId") Long userId);

    @Query("select t.priority, count(t) from Task t where t.project.id = :projectId group by t.priority")
    List<Object[]> countProjectByPriority(@Param("projectId") Long projectId);
}
