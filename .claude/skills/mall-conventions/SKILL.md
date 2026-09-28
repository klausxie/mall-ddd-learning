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
| 路径 | camelCase，例如 `/user/create`、`/address/create`、`/address/page` |
| 分页参数 | 固定 `curPage`（从 1 开始）、`pageSize`（默认 10） |
| 分页响应 | 统一 `common.model.Page<T>`：`{ curPage, pageSize, total, records }` |
| 统一响应 | 统一 `common.model.Response<T>`：`{ code, message, data }` |

命名正误对照：

| 场景 | 正确 | 错误 |
|---|---|---|
| 分页查询 | `GET /address/page?curPage=1&pageSize=10` | `/address/page?pageNumber=1` |
| 新增 | `POST /address/create` | `PUT /address/create` |
| 删除 | `POST /address/remove` | `DELETE /address/1` |
| 详情 | `GET /user/get` | `/user/get-detail` |

## 二、分页模板

请求对象继承 `Pageable`，直接复用 `curPage` / `pageSize` / `needTotal`：

```java
@Data
public class AddressPageRequest extends Pageable {

    public AddressPageCondition buildCondition() {
        return AddressPageCondition.builder()
                .offset(getOffset())
                .size(getPageSize())
                .build();
    }
}
```

领域层查询条件用 `offset` + `size`（**这是内部字段，不是对外参数名**），
对外一律 `curPage` / `pageSize`，两者在 `buildCondition()` 里转换。
需要过滤条件就往 request 和 condition 上各加一个同名字段。

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

违反会被 `ArchitectureTest` 拦下。另外两条容易忘的：

- 断言（`Assert` / `Asserts`）**只允许出现在 domain**；编排层报错抛 `ErrorCodeException`；
- `web` 只依赖 `application` + `common`，**不得直接引用 domain 的实体**（响应模型放 `application/.../response`）。

## 四、新建一个接口的完整清单

以"新增收货地址"为例（仓库里已有可对照的实现）：

- [ ] `application/user/command/request/AddressCreateRequest.java` —— 请求 DTO，`@Data` + 必填校验注解
- [ ] `application/user/query/response/AddressInfo.java` —— 响应模型，**不要把领域实体直接返回**
- [ ] 在 `UserCmdService` 加方法声明
- [ ] 在 `UserCmdServiceImpl` 实现，**只编排**，业务规则放 `domain`
- [ ] 业务规则写在实体方法（如 `Address.validate()` / `assertOwnedBy()`）、`UserValidator`、
      `XxxSpec`（判断是不是）或 `XxxPolicy`（算出一个值）
- [ ] `web/AddressController` 加 `@PostMapping("create")`，返回 `Response<AddressInfo>`
- [ ] 需要落库：改 Mapper 接口 + `resources/mapper/XxxMapper.xml` + 新增 `db/migration/V2__xxx.sql` 三者保持一致
- [ ] 分页接口才需要 `Pageable` / `Page`
- [ ] 补测试：领域规则写单测；端到端写 `*ApiTest`（真库、`@Transactional` 回滚）
- [ ] 跑 `./mvnw verify`

## 五、常用写法

```java
// 控制器：只做转发，不写逻辑；返回统一响应 + 响应模型
@PostMapping("create")
public Response<AddressInfo> createAddress(@Valid @RequestBody AddressCreateRequest request) {
    return Response.ok(userCmdService.createAddress(request));
}

// 依赖注入：构造器注入，不用 @Autowired
// application 层的 Service 用 @Service，assembler / domain / infrastructure 用 @Component
@Service
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;
}

// 校验失败：用 Asserts + 业务错误码，不要抛裸 RuntimeException
Asserts.state(present, UserErrorCode.ADDRESS_NOT_EXISTS);

// 实体自己守不变量；不变量写在方法里，而不是靠调用方自觉
public void validate() {
    Asserts.state(recipient != null && !recipient.isBlank(), UserErrorCode.RECIPIENT_IS_REQUIRED);
}

// 日志：@Slf4j，不要 System.out
log.warn("地址不存在: {}", addressId);
```
