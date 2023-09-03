package cn.mklaus.app.infrastructure.event;

import cn.mklaus.app.domain.common.EventPublisher;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/9/3
 */
@Slf4j
@Component
public class RocketmqEventPublisher implements EventPublisher {

    @Override
    public void publish(Object event) {
        log.info("publish data to rocketmq: {}", JSON.toJSONString(event));
    }

}
