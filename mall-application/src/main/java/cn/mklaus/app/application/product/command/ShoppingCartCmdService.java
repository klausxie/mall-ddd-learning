package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.ShoppingCartRemoveRequest;
import cn.mklaus.app.application.product.command.request.ShoppingCartSaveRequest;

/**
 * @author klausxie
 * @since 2023/8/25
 */
public interface ShoppingCartCmdService {

    void saveShoppingCard(ShoppingCartSaveRequest req);

    void removeShoppingCard(ShoppingCartRemoveRequest req);

}
