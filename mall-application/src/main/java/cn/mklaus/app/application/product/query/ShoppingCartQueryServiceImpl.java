package cn.mklaus.app.application.product.query;

import cn.mklaus.app.repostiory.product.query.ShoppingCartQuery;
import cn.mklaus.app.repostiory.product.query.model.ShoppingCartItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ShoppingCartQueryServiceImpl implements ShoppingCartQueryService {

    private final ShoppingCartQuery shoppingCartQuery;

    public List<ShoppingCartItem> listShoppingCart(long userId) {
        return shoppingCartQuery.listShoppingCart(userId);
    }

}