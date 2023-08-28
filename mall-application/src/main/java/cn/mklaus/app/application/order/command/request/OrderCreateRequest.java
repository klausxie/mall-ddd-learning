package cn.mklaus.app.application.order.command.request;

import lombok.Data;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/27
 */
@Data
public class OrderCreateRequest {

    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private Integer count;
    }

}
