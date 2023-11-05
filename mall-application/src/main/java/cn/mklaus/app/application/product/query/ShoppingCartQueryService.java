package cn.mklaus.app.application.product.query;

import cn.mklaus.app.domain.product.shoppingcart.query.model.ShoppingCartItemInfo;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/25
 */

public interface ShoppingCartQueryService {

    List<ShoppingCartItemInfo> listShoppingCart(long userId);

}
