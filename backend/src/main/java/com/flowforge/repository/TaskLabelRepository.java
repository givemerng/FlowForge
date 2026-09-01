package com.flowforge.repository;

import com.flowforge.entity.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {
    Optional<TaskLabel> findByTaskIdAndLabelId(Long taskId, Long labelId);
    void deleteByTaskIdAndLabelId(Long taskId, Long labelId);
}
