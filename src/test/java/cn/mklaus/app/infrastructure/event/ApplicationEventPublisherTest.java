package cn.mklaus.app.infrastructure.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 事件发布时机的语义：{@code publish} 立刻发；{@code publishAfterCommit} 有事务就等提交。
 *
 * <p>
 * 这里只断言"有没有把事件交给 Spring 事件总线"；MQ 投递在样例里是日志替身，不参与断言。
 *
 * @author klaus
 * @since 2026/9/29
 */
class ApplicationEventPublisherTest {

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final ApplicationEventPublisher publisher = new ApplicationEventPublisher(applicationContext);

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishShouldForwardImmediately() {
        publisher.publish("registered");

        verify(applicationContext).publishEvent("registered");
    }

    @Test
    void publishAfterCommitShouldForwardImmediatelyWithoutTransaction() {
        publisher.publishAfterCommit("registered");

        verify(applicationContext).publishEvent("registered");
    }

    @Test
    void publishAfterCommitShouldWaitForCommitWhenTransactionIsActive() {
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishAfterCommit("registered");
        verifyNoInteractions(applicationContext);

        TransactionSynchronizationManager.getSynchronizations()
            .forEach(TransactionSynchronization::afterCommit);

        verify(applicationContext).publishEvent("registered");
    }

}
