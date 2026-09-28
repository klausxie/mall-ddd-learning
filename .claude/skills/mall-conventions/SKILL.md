---
name: mall-conventions
description: mall 项目的开发规范细节。新增或修改接口、请求对象、领域模型、分页查询，或不确定新文件该放哪个包时使用。包含接口约定、分页模板、分层规则与新建功能清单。
---

# mall 项目开发规范

速查见 `CLAUDE.md`；目录职责与分层见 `ARCHITECTURE.md`。本文件放"动手时要照着抄"的细节。

## 一、接口约定

| 项 | 规则 |
|---|---|
| HTTP 方法 | **只用 GET / POST**。查询用 GET；新增、修改、删除用 POST + 路径动词 |
| 路径 | camelCase，例如 `/product/create`、`/product/onSale`、`/product/page` |
| 分页参数 | 固定 `curPage`（从 1 开始）、`pageSize`（默认 10） |
| 分页响应 | 统一 `common.model.Page<T>`：`{ curPage, pageSize, total, records }` |
| 统一响应 | 统一 `common.model.Response<T>`：`{ code, message, data }` |

命名正误对照：

| 场景 | 正确 | 错误 |
|---|---|---|
| 分页查询 | `GET /product/page?curPage=1&pageSize=10` | `/product/page?pageNumber=1` |
| 上架 | `POST /product/onSale` | `PUT /product/onSale` |
| 删除 | `POST /product/remove` | `DELETE /product/1` |
| 详情 | `GET /product/get` | `/product/get-detail` |

## 二、分页模板

请求对象继承 `Pageable`，直接复用 `curPage` / `pageSize` / `needTotal`：

```java
@Data
public class ProductPageRequest extends Pageable {

    private String keyword;
    private ProductStatus status;

    public ProductPageCondition buildCondition() {
        return ProductPageCondition.builder()
                .keyword(keyword)
                .status(status)
                .offset(getOffset())
                .size(getPageSize())
                .build();
    }
}
```

领域层查询条件用 `offset` + `size`（**这是内部字段，不是对外参数名**），
对外一律 `curPage` / `pageSize`，两者在 `buildCondition()` 里转换。

## 三、分层与依赖方向

```
web → application → domain → common
                     ▲
        infrastructure
```

写代码时的三条硬约束：

1. `application` 只做编排（组装领域对象、调领域服务），**不写业务规则**；
2. `domain` **不依赖** application / web / infrastructure，也不感知具体存储实现；
3. `infrastructure` 的实现通过 `domain` 里的接口暴露，由 Spring 注入。

违反会被 `ArchitectureTest` 拦下。

## 四、新建一个接口的完整清单

以 "商品下架" 为例：

- [ ] `application/product/command/request/ProductOffSaleRequest.java` —— 请求 DTO，`@Data`
- [ ] 在 `ProductCmdService` 加方法声明
- [ ] 在 `ProductCmdServiceImpl` 实现，**只编排**，业务规则放 `domain`
- [ ] 业务规则写在 `Product` 实体的方法（如 `becomeOffSale()`）或 `ProductValidator`
- [ ] `web/ProductController` 加 `@PostMapping("offSale")`
- [ ] 分页接口才需要 `Pageable` / `Page`
- [ ] 跑 `./mvnw verify`

## 五、常用写法

```java
// 控制器：只做转发，不写逻辑
@PostMapping("onSale")
public void onSaleProduct(@RequestBody ProductOnSaleRequest request) {
    productCmdService.onSaleProduct(request);
}

// 依赖注入：构造器注入，不用 @Autowired
// application 层的 Service 用 @Service，assembler / domain / infrastructure 用 @Component
@Service
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;
}

// 校验失败：用 Asserts + 业务错误码，不要抛裸 RuntimeException
Asserts.state(present, ProductErrorCode.PRODUCT_NOT_EXISTS);

// 日志：@Slf4j，不要 System.out
log.warn("商品不存在: {}", productId);
```
