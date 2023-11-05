package cn.mklaus.app.application.product.command.assembler;

import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.domain.product.product.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author klausxie
 * @since 2023/8/24
 */
@Component
@AllArgsConstructor
public class ProductAssembler {

    public Product buildProduct(ProductCreateRequest req) {
        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setContent(req.getContent());
        product.setCover(req.getCover());
        product.setPrice(req.getPrice());
        product.setInventory(req.getInventory());
        return product;
    }

    public Product buildProduct(ProductUpdateRequest req) {
        Product product = new Product();
        product.setId(req.getProductId());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setContent(req.getContent());
        product.setCover(req.getCover());
        return product;
    }

}
