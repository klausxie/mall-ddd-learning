package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.*;
import cn.mklaus.app.domain.product.product.Product;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductCmdService {

    Product createProduct(ProductCreateRequest req);

    void updateProduct(ProductUpdateRequest req);

    void removeProduct(ProductRemoveRequest req);

    void onSaleProduct(ProductOnSaleRequest req);

    void offSaleProduct(ProductOffSaleRequest req);

}
