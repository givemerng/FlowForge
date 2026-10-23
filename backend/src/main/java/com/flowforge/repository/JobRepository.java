package com.flowforge.repository;

import com.flowforge.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(Job.Status status);
    Optional<Job> findByIdempotencyKey(String idempotencyKey);

    @Query("select distinct j from Report r join r.job j left join ProjectMember pm on pm.project = r.project " +
            "where r.project.owner.id = :userId or pm.user.id = :userId order by j.createdAt desc")
    List<Job> findAccessibleByUserId(@Param("userId") Long userId, Pageable pageable);
}
