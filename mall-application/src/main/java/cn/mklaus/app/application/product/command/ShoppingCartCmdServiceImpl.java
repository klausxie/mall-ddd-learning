package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.ShoppingCartRemoveRequest;
import cn.mklaus.app.application.product.command.request.ShoppingCartSaveRequest;
import cn.mklaus.app.common.auth.Context;
import cn.mklaus.app.domain.product.product.ProductValidator;
import cn.mklaus.app.domain.product.shoppingcart.ShoppingCart;
import cn.mklaus.app.domain.product.shoppingcart.ShoppingCartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Service
@AllArgsConstructor
public class ShoppingCartCmdServiceImpl implements ShoppingCartCmdService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductValidator productValidator;

    @Override
    public void saveShoppingCard(ShoppingCartSaveRequest req) {
        productValidator.assertProductExists(req.getProductId());
        Assert.state(req.getCount() > 0, "商品数量不能小于1");

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(Context.currentOperator().getId());
        shoppingCart.setProductId(req.getProductId());
        shoppingCart.setCount(req.getCount());

        shoppingCartRepository.saveShoppingCart(shoppingCart);
    }

    @Override
    public void removeShoppingCard(ShoppingCartRemoveRequest req) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(Context.currentOperator().getId());
        shoppingCart.setProductId(req.getProductId());
        shoppingCartRepository.removeShoppingCart(shoppingCart);
    }

}
