package com.flowforge.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class JobQueuePublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${flowforge.rabbitmq.jobs-queue:flowforge.jobs}")
    private String jobsQueue;

    public JobQueuePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAfterCommit(String payload) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(payload);
                }
            });
            return;
        }
        publish(payload);
    }

    private void publish(String payload) {
        rabbitTemplate.convertAndSend(jobsQueue, payload);
    }
}
