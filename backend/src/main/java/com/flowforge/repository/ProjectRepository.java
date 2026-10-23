package com.flowforge.repository;

import com.flowforge.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Override
    @EntityGraph(attributePaths = "owner")
    List<Project> findAll();

    @EntityGraph(attributePaths = "owner")
    List<Project> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    @Query("select distinct p from Project p left join ProjectMember pm on pm.project = p " +
            "where p.owner.id = :userId or pm.user.id = :userId")
    List<Project> findAccessibleByUserId(@Param("userId") Long userId);
}
