package cn.mklaus.app.application.product.command;

import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductOffSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductOnSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductRemoveRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.application.product.query.response.ProductInfo;

/**
 * @author klausxie
 * @since 2023/8/24
 */
public interface ProductCmdService {

    ProductInfo createProduct(ProductCreateRequest req);

    void updateProduct(ProductUpdateRequest req);

    void removeProduct(ProductRemoveRequest req);

    void onSaleProduct(ProductOnSaleRequest req);

    void offSaleProduct(ProductOffSaleRequest req);

}
