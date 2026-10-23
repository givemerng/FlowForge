package com.flowforge.repository;

import com.flowforge.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    @Override
    @EntityGraph(attributePaths = {"project", "project.owner", "createdBy", "job"})
    List<Report> findAll();

    @EntityGraph(attributePaths = {"project", "project.owner", "createdBy", "job"})
    List<Report> findByProjectId(Long projectId);

    @EntityGraph(attributePaths = {"project", "project.owner", "createdBy", "job"})
    List<Report> findByProjectIdIn(List<Long> projectIds);

    @EntityGraph(attributePaths = {"project", "project.owner", "createdBy", "job"})
    @Query("select distinct r from Report r left join ProjectMember pm on pm.project = r.project " +
            "where r.project.owner.id = :userId or pm.user.id = :userId")
    List<Report> findAccessibleByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "project.owner", "createdBy", "job"})
    Optional<Report> findByJobId(Long jobId);
}
