package cn.mklaus.app.application.product.query;

import cn.mklaus.app.repostiory.product.query.model.ShoppingCartItem;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/25
 */

public interface ShoppingCartQueryService {

    List<ShoppingCartItem> listShoppingCart(long userId);

}
