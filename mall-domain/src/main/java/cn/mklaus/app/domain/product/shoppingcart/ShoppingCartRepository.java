package cn.mklaus.app.domain.product.shoppingcart;

import cn.mklaus.app.domain.product.shoppingcart.query.model.ShoppingCartItem;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface ShoppingCartRepository {

    void saveShoppingCart(ShoppingCart shoppingCart);

    void removeShoppingCart(ShoppingCart shoppingCart);

    List<ShoppingCartItem> listShoppingCart(long userId);

}
