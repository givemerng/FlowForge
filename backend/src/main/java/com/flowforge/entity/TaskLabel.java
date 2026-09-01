package com.flowforge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "task_labels", uniqueConstraints = {
        @UniqueConstraint(name = "uk_task_label", columnNames = {"task_id", "label_id"})
})
public class TaskLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public Label getLabel() { return label; }
    public void setLabel(Label label) { this.label = label; }
}
