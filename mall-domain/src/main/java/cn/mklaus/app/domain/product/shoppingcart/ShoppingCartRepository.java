package cn.mklaus.app.domain.product.shoppingcart;

/**
 * @author klausxie
 * @since 2023/8/16
 */
public interface ShoppingCartRepository {

    void saveShoppingCart(ShoppingCart shoppingCart);

    void removeShoppingCart(ShoppingCart shoppingCart);

}
