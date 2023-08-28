package cn.mklaus.app.domain.order;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Component
public class OrderNoGenerator {

    public String nextOrderNo() {
        return UUID.randomUUID().toString();
    }

}
