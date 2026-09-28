package cn.mklaus.app.infrastructure.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 领域事件的发布时机：没有事务立刻发；有事务则推迟到提交之后。
 *
 * @author klaus
 * @since 2026/9/29
 */
class AfterCommitEventPublisherTest {

    private final RecordingEventPublisher delegate = new RecordingEventPublisher();
    private final AfterCommitEventPublisher publisher = new AfterCommitEventPublisher(delegate);

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldPublishImmediatelyWithoutTransaction() {
        publisher.publish("registered");

        assertEquals(List.of("registered"), delegate.published);
    }

    @Test
    void shouldDeferUntilCommitWhenTransactionIsActive() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publish("registered");
        assertTrue(delegate.published.isEmpty(), "提交前不应该发出去");

        TransactionSynchronizationManager.getSynchronizations()
            .forEach(TransactionSynchronization::afterCommit);

        assertEquals(List.of("registered"), delegate.published, "提交后才发");
    }

    private static class RecordingEventPublisher implements cn.mklaus.app.domain.common.EventPublisher {

        private final List<Object> published = new ArrayList<>();

        @Override
        public void publish(Object event) {
            published.add(event);
        }

    }

}
