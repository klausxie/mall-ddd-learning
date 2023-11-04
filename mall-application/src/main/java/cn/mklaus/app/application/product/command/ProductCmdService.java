package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.*;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductCmdService {

    ProductDTO createProduct(ProductCreateRequest req);

    void updateProduct(ProductUpdateRequest req);

    void removeProduct(ProductRemoveRequest req);

    void onSaleProduct(ProductOnSaleRequest req);

    void offSaleProduct(ProductOffSaleRequest req);

}
