package cn.mklaus.app.application.product.command.assembler;

import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.model.ProductDTO;
import cn.mklaus.app.domain.product.category.CategoryValidator;
import cn.mklaus.app.domain.product.product.Product;
import cn.mklaus.app.domain.product.product.ProductStatus;
import cn.mklaus.app.domain.product.product.ProductValidator;
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

    private final CategoryValidator categoryValidator;
    private final ProductValidator productValidator;

    public Product buildProduct(ProductCreateRequest req) {
        categoryValidator.assertCategoryExists(req.getCategoryId());

        Product product = new Product();
        product.setStatus(ProductStatus.PENDING);
        product.setCategoryId(req.getCategoryId());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setContent(req.getContent());
        product.setCover(req.getCover());

        product.updatePrice(req.getPrice());
        product.updateInventory(req.getInventory());
        productValidator.assertProductNameCanUse(product);
        return product;
    }

    public ProductDTO toProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }

}
