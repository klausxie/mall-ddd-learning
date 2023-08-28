package cn.mklaus.app.application.product.query.repository;

import cn.mklaus.app.application.product.query.repository.model.ShoppingCartItem;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/25
 */
public interface ShoppingCartQuery {

    List<ShoppingCartItem> listShoppingCart(Long userId);

}
