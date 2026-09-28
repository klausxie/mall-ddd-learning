# 架构说明（mall）

本文是**结构规范的唯一事实来源**。`CLAUDE.md` 只写"怎么做"，细节在这里。

> 目录怎么摆很难自动检查，但"谁不许依赖谁"是可以的——所以下文的**分层规则**同时写进了
> `src/test/java/cn/mklaus/app/ArchitectureTest.java`，违反会直接让 `mvn verify` 失败。

---

## 一、目录结构与职责

```
src/main/java/cn/mklaus/app/
├── Application.java              启动类
│
├── web/                          【最外层】HTTP 入口
│   └── XxxController.java            只做参数接收 + 调用 application，不写业务逻辑
│
├── application/                  【应用层】编排用例、事务边界
│   └── <业务>/
│       ├── command/                  写操作（增删改）
│       │   ├── XxxCmdService.java            接口
│       │   ├── XxxCmdServiceImpl.java        实现
│       │   ├── assembler/                    请求对象 → 领域对象
│       │   ├── request/                      写操作请求 DTO
│       │   └── response/                     写操作响应模型（如注册结果）
│       └── query/                    读操作
│           ├── XxxQueryService.java
│           ├── XxxQueryServiceImpl.java
│           ├── request/                      查询请求 DTO
│           └── response/                     响应模型 XxxInfo（领域实体不直接出接口）
│
├── domain/                       【核心层】业务规则与领域模型，不感知框架与存储
│   ├── common/                               跨业务能力接口（验证码 / 事件 / 密码哈希）
│   └── <业务>/
│       ├── User / Product / ...              实体，自带永远成立的不变量校验方法
│       ├── XxxValidator.java                 单实体校验（需要查库的规则放这里）
│       ├── XxxErrorCode.java                 本业务的错误码与信息模板
│       ├── spec/                             按用例生效的可组合约束 XxxSpec
│       ├── <能力>/XxxPolicy.java             条件 → 数值的业务策略
│       ├── XxxService.java                   跨实体的领域服务
│       ├── XxxMapper.java                    持久化接口（⚠️ 见"已知偏差"）
│       └── query/                            查询条件与只读模型
│
├── infrastructure/               【最外层】外部系统的具体实现
│   ├── captcha/                              短信验证码
│   ├── event/                                消息队列
│   └── security/                             密码哈希等安全能力实现
│
├── common/                       【公共设施】零业务依赖
│   ├── auth/                                 Context / Operator
│   ├── exception/                            错误码、统一异常、Violation
│   ├── model/                                Response / Page / Pageable
│   └── spec/                                 Spec 与 Specs（规格抽象，零业务依赖）
│
└── configuration/                框架配置（异常处理器等）
```

## 二、新文件放哪里

| 我要写的东西 | 放哪 | 不要放哪 |
|---|---|---|
| HTTP 接口 | `web/XxxController` | application / domain |
| 一个业务用例的编排 | `application/<业务>/command` 或 `query` | domain |
| 请求参数对象 | `application/<业务>/{command,query}/request` | domain |
| 响应模型（出参） | `application/<业务>/{command,query}/response/XxxInfo` | 直接把领域实体当响应体 |
| 请求对象 → 领域对象的转换 | `application/<业务>/command/assembler` | 实体内部 |
| 永远成立的实体不变量 | 该实体的 `validate()` | Spec（会被无关用例误用） |
| 按用例生效、可复用的业务约束 | `domain/<业务>/spec/XxxSpec` | 实体方法 / Controller |
| 条件 → 数值的业务策略（积分 / 运费 / 折扣） | `domain/<业务>/<能力>/XxxPolicy` | 硬编码在 application |
| 需要查库的校验规则 | `domain/<业务>/XxxValidator` | Spec（规格必须保持无状态） |
| 业务错误码 | `domain/<业务>/XxxErrorCode` | 硬编码字符串 |
| 跨实体业务规则 | `domain/<业务>/XxxService` | application（application 只编排） |
| 数据库访问接口 | `domain/<业务>/XxxMapper` + `resources/mapper/XxxMapper.xml` | application |
| 调外部系统（短信、MQ、缓存） | `infrastructure/<能力>/`，通过 `domain` 里的接口暴露 | domain 里直接 new |
| 统一响应 / 错误码 / 分页模型 / 规格抽象 | `common/model`、`common/exception`、`common/spec` | 各业务包各写一份 |
| 只有一处用到的工具方法 | **就地放在使用它的包里** | 提前抽到 common |

> `common/` 不接受带业务含义的类。它一旦依赖 `domain`，ArchUnit 会直接报错。

### 规格（Specification）怎么用

`Spec<T>`（`common/spec`）把一条业务约束建模成具名、可组合、可单独测试的对象；
不满足时给出「违规原因」（错误码 + 信息模板参数），由 `Specs.assertSatisfied` 转成统一的
`ErrorCodeException`，因此错误码和信息都由规则自己拥有，调用方只负责决定"什么时候查"。

- **该用**：同一条规则要被多个用例引用（"必须成年"注册、下单、领券都要查），
  或规则需要按业务拼装（`and` / `or` / `not`）。
- **不该用**：永远成立的实体不变量 → 实体 `validate()`；
  一次性检查 → `Asserts.state(condition, errorCode, args...)`；需要查库 → `XxxValidator`。
- **禁止 spec-as-query**：规格只表达纯业务规则，不得生成 SQL / MyBatis-Plus 条件，
  否则 `domain` 会感知持久化实现（`ArchitectureTest` 直接报错）。
  查询条件继续放 `domain/<业务>/query`。
- 规格无状态，调用方直接 `new XxxSpec()`；一旦需要注入依赖，说明它应该待在 `XxxValidator` 里。
- 新规格 = 一个 `XxxSpec` 类 + 一个 `XxxErrorCode` 枚举项 + 一个单测。
- **Spec 管"是不是"，Policy 管"是多少"**：像"低于 30 岁送 200、否则送 300"这种
  条件 → 数值的规则没有失败态，用 `XxxPolicy` 承载，写成 Spec 会把返回值语义搞混。
  Policy 同样是纯计算、无依赖、直接 `new`，配单测钉住分档边界。

## 三、分层规则（由 ArchUnit 强制）

```
web ──────────► application ──────────► domain ──────────► common
 │                                        ▲
 └────────────────────────────────────────┘
                     infrastructure ──────┘
```

| 规则 | 含义 |
|---|---|
| `domain` 不得依赖 `application` / `web` / `infrastructure` | 核心层不反向依赖 |
| `domain` + `application` 不得依赖 `infrastructure` | 实现细节只能经 `domain` 的接口注入 |
| `web` 不得被任何内部层依赖 | 最外层只出不进 |
| `common` 不得依赖任何业务包 | 防止退化成垃圾场 |
| 顶层包之间不得有循环依赖 | 分层失效的早期信号 |
| `*Mapper` 必须是接口 | 实现由 MyBatis 生成 |
| 实现 `Spec` 的类必须以 `Spec` 结尾且位于 `..spec..` 包 | 规格统一命名与位置 |
| `@RestController` 必须在 `web` 包 | HTTP 入口集中管理 |
| `application` + `web` 不得使用 `Assert` / `Asserts` | 断言属于领域校验：编排层只能调 domain 的 Validator / Spec / 实体方法，报错就抛 `ErrorCodeException` |
| request 对象不得出现 `limit` / `offset` / 历史分页名 | 对外分页参数只有 `curPage` / `pageSize` |
| MyBatis API 只能出现在 `*Mapper` 接口上 | 见"已知偏差" |

## 四、已知偏差（有意保留，勿顺手"修掉"）

1. **MyBatis 注解出现在 `domain` 包。**
   本次重构把 `XxxRepository` 改名为 `XxxMapper` 并加了 `@Mapper`，接口就落在 `domain` 里。
   严格 DDD 应该把映射器放在 `infrastructure/repository`，此处按现状接受，
   ArchUnit 规则已把例外收窄到"以 `Mapper` 结尾的接口"。
   如果以后要纠正，请把接口移回 `infrastructure`，并在 `domain` 保留纯业务接口。

2. **`domain` 依赖 Spring 的 stereotype 注解**（`@Component`、`@AllArgsConstructor`）。
   目前未强制剥离。若要做到框架无关，需要引入显式 `@Configuration` 装配类。
   注意：`Spec` / `Policy` 这类纯规则是普通类、直接 `new`，**不要**再给它们加 `@Component`，
   免得偏差继续扩大。

## 五、测试分层

| 测试 | 需要数据库 | 覆盖 |
|---|---|---|
| `ArchitectureTest` | 否 | 分层、依赖方向、命名、断言归属等架构规则 |
| `MapperStatementsTest` | 否 | Mapper 接口方法 ↔ XML statement 一一对账 |
| `ApplicationContextTest` | 否 | Bean 装配 + Mapper XML 解析 + Controller 注册 |
| 领域 / 基础设施单测 | 否 | Spec / Policy / 密码哈希 / 分页 / 上下文 |
| `MapperSmokeTest` | 是 | 真库 SQL：`schema.sql` 对齐、自增回填、枚举往返、分页 |
| `UserRegisterApiTest`、`AddressApiTest`、`ProductApiTest` | 是 | HTTP 端到端：注册送积分、地址增删改查、商品上下架 |

- `./mvnw verify` 只跑前四类，几秒出结果，**不需要数据库**；
- 真库测试默认跳过，由 `MALL_DB_PASSWORD` 打开，且都带 `@Transactional`，跑完自动回滚；
- 覆盖率门槛在 `coverage-check` profile 里（行覆盖 ≥ 80%），只跟真库测试一起跑：
  `MALL_DB_PASSWORD=xxx ./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local`，
  报告在 `target/site/jacoco/index.html`；
- CI（`.github/workflows/verify.yml`）用 MySQL service container 跑全量，包括真库测试与覆盖率门槛。

"哪些是样例替身、真实项目该怎么做"见 [README.md](README.md) 的样例替身清单。

## 六、修改架构的正确流程

1. 先改本文（目录树 / 决策表 / 分层规则）。
2. 同步更新 `ArchitectureTest`。
3. 再动代码。
4. `./mvnw verify` 必须全绿。
