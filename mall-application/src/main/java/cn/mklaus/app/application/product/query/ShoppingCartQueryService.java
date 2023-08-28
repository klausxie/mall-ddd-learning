package cn.mklaus.app.application.product.query;

import cn.mklaus.app.application.product.query.repository.ShoppingCartQuery;
import cn.mklaus.app.application.product.query.repository.model.ShoppingCartItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Component
@AllArgsConstructor
public class ShoppingCartQueryService {

    private final ShoppingCartQuery shoppingCartQuery;

    public List<ShoppingCartItem> listShoppingCart(long userId) {
        return shoppingCartQuery.listShoppingCart(userId);
    }

}
