package cn.mklaus.app.domain.common;

/**
 * @author klausxie
 * @since 2023/8/28
 */
public interface EventPublisher {

    void publish(Object event);

}
