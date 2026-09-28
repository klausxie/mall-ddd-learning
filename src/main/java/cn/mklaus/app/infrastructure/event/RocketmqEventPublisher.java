package cn.mklaus.app.infrastructure.event;

import cn.mklaus.app.domain.common.EventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息发布者的样例替身：真实实现应把事件投递到 RocketMQ，这里只打日志。
 *
 * @author klausxie
 * @since 2023/9/3
 */
@Slf4j
@Component
@AllArgsConstructor
public class RocketmqEventPublisher implements EventPublisher {

    private final ObjectMapper objectMapper;

    @Override
    public void publish(Object event) {
        log.info("publish data to rocketmq: {}", toJson(event));
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("事件序列化失败，降级为 toString: {}", event, e);
            return String.valueOf(event);
        }
    }

}
