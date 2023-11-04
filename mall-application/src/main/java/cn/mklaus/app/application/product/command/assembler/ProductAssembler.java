package cn.mklaus.app.application.product.command.assembler;

import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
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
        product.setCategoryId(req.getCategoryId());
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
        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setContent(req.getContent());
        product.setCover(req.getCover());
        return product;
    }

    public ProductDTO toProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }

}
