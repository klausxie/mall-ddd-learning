package cn.mklaus.app.web;

import cn.mklaus.app.application.product.command.ProductCmdService;
import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductOnSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.application.product.query.ProductQueryService;
import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.repostiory.product.query.model.ProductDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * @author klausxie
 * @since 2023/11/4
 */
@RestController
@RequestMapping("product")
@AllArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductCmdService productCmdService;

    @PostMapping("create")
    public ProductDTO createProduct(@RequestBody ProductCreateRequest request) {
        return productCmdService.createProduct(request);
    }

    @PostMapping("update")
    public void createProduct(@RequestBody ProductUpdateRequest request) {
        productCmdService.updateProduct(request);
    }

    @PostMapping("onSale")
    public void onSaleProduct(@RequestBody ProductOnSaleRequest request) {
        productCmdService.onSaleProduct(request);
    }

    @GetMapping("page")
    public Page<ProductDTO> pageProduct(ProductPageRequest request) {
        return productQueryService.pageProduct(request);
    }

}
