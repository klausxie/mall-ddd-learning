package cn.mklaus.app.infrastructure.event;

import cn.mklaus.app.domain.common.EventPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 消息发布者的样例替身：真实实现应把事件投递到 RocketMQ，这里只打日志。
 *
 * @author klausxie
 * @since 2023/9/3
 */
@Slf4j
@Component
@AllArgsConstructor
public class ApplicationEventPublisher implements EventPublisher {

    private final ApplicationContext applicationContext;

    @Override
    public void publish(Object event) {
        log.info("publish data to rocketmq: {}", event);
        applicationContext.publishEvent(event);
    }

    @Override
    public void publishAfterCommit(Object event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            applicationContext.publishEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                applicationContext.publishEvent(event);
            }
        });
    }

}
