package cn.mklaus.app.repostiory.product.query;


import cn.mklaus.app.repostiory.product.query.model.ShoppingCartItem;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/25
 */
public interface ShoppingCartQuery {

    List<ShoppingCartItem> listShoppingCart(Long userId);

}
