package cn.mklaus.app.application.product.query;

import cn.mklaus.app.domain.product.shoppingcart.ShoppingCartRepository;
import cn.mklaus.app.domain.product.shoppingcart.query.model.ShoppingCartItem;
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

    private final ShoppingCartRepository shoppingCartRepository;

    public List<ShoppingCartItem> listShoppingCart(long userId) {
        return shoppingCartRepository.listShoppingCart(userId);
    }

}