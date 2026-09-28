package cn.mklaus.app.web;

import cn.mklaus.app.application.product.command.ProductCmdService;
import cn.mklaus.app.application.product.command.request.ProductCreateRequest;
import cn.mklaus.app.application.product.command.request.ProductOffSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductOnSaleRequest;
import cn.mklaus.app.application.product.command.request.ProductRemoveRequest;
import cn.mklaus.app.application.product.command.request.ProductUpdateRequest;
import cn.mklaus.app.application.product.query.ProductQueryService;
import cn.mklaus.app.application.product.query.request.ProductPageRequest;
import cn.mklaus.app.application.product.query.response.ProductInfo;
import cn.mklaus.app.common.model.Page;
import cn.mklaus.app.common.model.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Response<ProductInfo> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return Response.ok(productCmdService.createProduct(request));
    }

    @PostMapping("update")
    public Response<Void> updateProduct(@RequestBody ProductUpdateRequest request) {
        productCmdService.updateProduct(request);
        return Response.ok();
    }

    @PostMapping("remove")
    public Response<Void> removeProduct(@RequestBody ProductRemoveRequest request) {
        productCmdService.removeProduct(request);
        return Response.ok();
    }

    @PostMapping("onSale")
    public Response<Void> onSaleProduct(@RequestBody ProductOnSaleRequest request) {
        productCmdService.onSaleProduct(request);
        return Response.ok();
    }

    @PostMapping("offSale")
    public Response<Void> offSaleProduct(@RequestBody ProductOffSaleRequest request) {
        productCmdService.offSaleProduct(request);
        return Response.ok();
    }

    @GetMapping("page")
    public Response<Page<ProductInfo>> pageProduct(ProductPageRequest request) {
        return Response.ok(productQueryService.pageProduct(request));
    }

}
