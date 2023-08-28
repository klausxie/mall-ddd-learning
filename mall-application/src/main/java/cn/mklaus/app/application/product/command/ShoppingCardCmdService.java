package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.ShoppingCardRemoveRequest;
import cn.mklaus.app.application.product.command.request.ShoppingCartSaveRequest;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.domain.product.product.ProductValidator;
import cn.mklaus.app.domain.product.shoppingcart.ShoppingCart;
import cn.mklaus.app.domain.product.shoppingcart.ShoppingCartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/8/25
 */
@Component
@AllArgsConstructor
public class ShoppingCardCmdService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductValidator productValidator;

    public void saveShoppingCard(ShoppingCartSaveRequest req) {
        productValidator.assertProductExists(req.getProductId());
        Assert.state(req.getCount() > 0, "商品数量不能小于1");

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(Context.currentOperator().getId());
        shoppingCart.setProductId(req.getProductId());
        shoppingCart.setCount(req.getCount());

        shoppingCartRepository.saveShoppingCart(shoppingCart);
    }

    public void removeShoppingCard(ShoppingCardRemoveRequest req) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(Context.currentOperator().getId());
        shoppingCart.setProductId(req.getProductId());
        shoppingCartRepository.removeShoppingCart(shoppingCart);
    }

}
