package com.flowforge.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class JobQueuePublisherTest {
    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final JobQueuePublisher publisher = new JobQueuePublisher(rabbitTemplate);

    @AfterEach
    void clearTransactionState() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void publishesOnlyAfterTheDatabaseTransactionCommits() {
        ReflectionTestUtils.setField(publisher, "jobsQueue", "flowforge.jobs");
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishAfterCommit("payload");

        verifyNoInteractions(rabbitTemplate);
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(rabbitTemplate).convertAndSend("flowforge.jobs", "payload");
    }
}
