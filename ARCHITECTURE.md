# 架构说明（mall）

本文是**结构规范的唯一事实来源**。`CLAUDE.md` 只写"怎么做"，细节在这里。

> **这个仓库是模板**：把它初始化成新项目的方式、改名清单、初始化后必须确认的事，
> 见 [TEMPLATE.md](TEMPLATE.md)；改名脚本是 [scripts/init.sh](scripts/init.sh)。
> 注意 `ArchitectureTest` 的包名从 `Application` 推导，**不要在规则里写死包名**，否则改名必漏。

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
│       │   ├── XxxCmdService.java            @Service + @Transactional，只编排
│       │   ├── XxxRequest.java               写操作请求 DTO（与 Service 同包）
│       │   └── XxxResponse.java              写操作响应模型（如注册结果）
│       └── query/                    读操作
│           ├── XxxQueryService.java
│           ├── XxxPageRequest.java           查询请求 DTO（与 Service 同包）
│           └── XxxInfo.java                  响应模型（record；领域实体不直接出接口）
│
├── domain/                       【核心层】业务规则与领域模型，不感知框架与存储
│   ├── common/                               跨业务能力接口（验证码 / 事件 / 密码哈希）
│   └── <业务>/
│       ├── User / Address / ...              实体，自带永远成立的不变量校验方法
│       ├── XxxValidator.java                 单实体校验（需要查库的规则放这里）
│       ├── XxxErrorCode.java                 本业务的错误码与信息模板
│       ├── spec/                             按用例生效的具名约束 XxxSpec
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

### 一次请求的调用链（照这条线读代码最快）

以 `POST /address/create` 为例，从 HTTP 到表一共 7 个点，每格只管一件事：

```
POST /address/create
└─ web/AddressController.createAddress              只转发；@Valid 管请求体格式
   └─ application/user/command/UserCmdService   编排 + 事务边界（@Transactional 在类上）
      ├─ UserCmdService#buildAddress()              request → 领域对象（私有方法，不做业务判断）
      ├─ domain/user/Address.validate()             实体不变量：永远成立的那些
      │  └─ common/exception/Asserts.state()        不满足就抛 ErrorCodeException（自带业务错误码）
      └─ domain/user/AddressMapper.saveAddress()    只声明接口，SQL 在 resources/mapper/*.xml
         └─ db/migration/V1__init_schema.sql        表结构；列必须与 resultMap 一一对应
```

读接口走 `UserQueryService` + `.../query/AddressInfo`（响应模型不出领域实体）；
失败出口统一在 `configuration/GlobalExceptionHandler`（HTTP 状态恒 200，靠 body.code 区分）。

按这条线找不到某条规则时，按"规则种类"对号入座：实体不变量 → 实体自身的 `validate()`；
按用例生效的约束 → `domain/<业务>/spec`；条件 → 数值 → `domain/<业务>/<能力>/*Policy`；
需要查库的校验 → `domain/<业务>/*Validator`。

## 二、新文件放哪里

| 我要写的东西 | 放哪 | 不要放哪 |
|---|---|---|
| HTTP 接口 | `web/XxxController` | application / domain |
| 一个业务用例的编排 | `application/<业务>/command` 或 `query` | domain |
| 请求参数对象 | `application/<业务>/{command,query}`（与 Service 同包） | domain |
| 响应模型（出参） | `application/<业务>/{command,query}/XxxInfo`（用 record） | 直接把领域实体当响应体 |
| 请求对象 → 领域对象的转换 | 需要容器协作者 → Service 的私有方法；纯字段映射 → 模型上的 `of()` | 实体内部、独立 assembler 类 |
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

### 新建一个接口要动哪几处（照顺序过一遍）

以"新增收货地址"为例，仓库里有可对照的完整实现：

1. `application/<业务>/{command,query}/XxxRequest.java` —— 请求 DTO（`@Data` + 校验注解）
2. `application/<业务>/{command,query}/XxxInfo.java` —— 响应模型（record），**不要把领域实体直接返回**
3. 在 `application/<业务>/.../XxxCmdService` 加方法，**只编排**（只有一个实现时不拆接口/实现）
4. 业务规则写进实体方法（`validate()` / `assertOwnedBy()`）、`XxxValidator`、`XxxSpec`（是不是）或 `XxxPolicy`（是多少）
5. `web/XxxController` 加 `@PostMapping("create")`，返回 `Response<XxxInfo>`
6. 需要落库：Mapper 接口 + `resources/mapper/XxxMapper.xml`（resultMap / INSERT / UPDATE 都要改）
   + `db/migration/V2__xxx.sql`。三处对不上会被 `MapperStatementsTest` / `EntityMappingTest` 拦下
7. 分页接口才需要 `Pageable` / `Page`
8. 补测试：领域规则写单测（有 domain 覆盖率地板兜底）；端到端写 `*ApiTest`（真库、`@Transactional` 回滚）
9. 跑 `./mvnw verify`

### 规格（Specification）怎么用

`Spec<T>`（`common/spec`）把一条业务约束建模成具名、可单独测试的对象；
不满足时给出「违规原因」（错误码 + 信息模板参数），由 `Specs.assertSatisfied` 转成统一的
`ErrorCodeException`，因此错误码和信息都由规则自己拥有，调用方只负责决定"什么时候查"。

- **该用**：同一条规则要被多个用例引用（"必须成年"注册、下单、领券都要查）。
  接口只有一个 `violation(T)`；确实需要把多条约束拼装时再加组合子，别提前预置没人用的 API。
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
| `web` 不得依赖 `domain` | 领域实体不出接口，返回数据用 `application/<业务>/...` 里的响应模型 |
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

3. **实体保留 Lombok `@Data` 生成的访问器，不做全面封装。**
   这是**有意的取舍**，不是遗漏：本项目的实体（`User` / `Address`）本身几乎不携带不变量——
   真正的规则住在值对象（`Mobile` 构造即校验）、`XxxValidator`、`XxxSpec`、`XxxPolicy` 和实体自己的行为方法里。
   全面去 setter 要把 MyBatis 改成构造器映射，从此**每加一个字段**都要同步构造器与 XML，
   属于持续摩擦；而模板的高频操作恰恰是"加字段"。
   策略是：**能在类型上免费守住的就守（值对象），守不住的靠 domain 里的规则 + 测试**。
   将来某个实体真的积累了不变量（状态机、金额、有效期），再单独封装它，收益才成立。

4. **跨限界上下文的依赖规则暂未启用。**
   模板目前只有一个上下文（`user`），"上下文之间不得互相引用实体"这类规则加了也是空转。
   等新项目长出第二个域时，在 `ArchitectureTest` 里补一条 `domain.<A>` 不得依赖 `domain.<B>` 即可。

## 五、测试分层

| 测试 | 需要数据库 | 覆盖 |
|---|---|---|
| `ArchitectureTest` | 否 | 分层、依赖方向、命名、断言归属等架构规则 |
| `GuardrailsTest` | 否 | 护栏自检：真的跑一遍 Checkstyle 验证规则会报错，外加 pom / CI 参数没被改弱 |
| `EntityMappingTest` | 否 | 实体字段 ↔ resultMap / INSERT / UPDATE ↔ 建表列 对账 |
| `MapperStatementsTest` | 否 | Mapper 接口方法 ↔ XML statement 一一对账 |
| `ApplicationContextTest` | 否 | Bean 装配 + Mapper XML 解析 + Controller 注册 |
| 领域 / 基础设施单测 | 否 | Spec / Policy / 密码哈希 / 分页 / 上下文 |
| `MapperSmokeTest` | 是 | 真库 SQL：与 Flyway 建的表对齐、自增回填、分页 |
| `UserRegisterApiTest`、`AddressApiTest` | 是 | HTTP 端到端：注册送积分、地址增删改查与分页 |

- `./mvnw verify` 只跑前四类，几秒出结果，**不需要数据库**；
- **封顶（两个口径，分别报数）**：
  - **检查类护栏**（`GuardrailsTest` + `EntityMappingTest` + `.claude/hooks` + 探针样本）**≤ 主代码 40%**
    （当前 564 / 1732 ≈ 32%）。要加一条新检查，先合并或删掉一条价值更低的；
  - **测试基础设施**（`support/`：真库置备 + 标记注解）**≤ 15%**（当前 201 / 1732 ≈ 11%）。
    它不算护栏（不检查规范，只负责把库准备好），单独设限是为了别让它无限膨胀；
  - `scripts/`（`init.sh` / `facts.sh`）是工具，两个口径都不计入——既不检查规范，也不置备环境；
- 真库测试（标了 `@RequiresRealDatabase` 的类）的数据源由 `support/RealDatabaseProvisioner` **三级置备**：
  ① 显式配置（环境变量 / profile 文件）→ ② 本机 Docker 可用就起 `mysql:8.0` 容器 → ③ 都没有则 H2(MODE=MySQL)。
  三级跑的都是同一份 `db/migration/V1__init_schema.sql`；选到 ③ 时打 WARN，**真 MySQL 由 CI 的 service container 校验**。
  它们都带 `@Transactional`，跑完自动回滚；
- 覆盖率分两档，报告都在 `target/site/jacoco/index.html`：
  - **默认 verify**：`domain` 每个包的行覆盖 ≥ 70%（`jacoco.domain.line.coverage.min`）。
    domain 是纯规则、单测就能覆盖，这条地板只依赖单测（真库用例跑不跑都成立）；逐包评估，
    "新加一个 domain 包却没写单测"藏不住；
  - **`coverage-check` profile**：全局行覆盖 ≥ 80%（`jacoco.line.coverage.min`），带真库测试跑，
    CI 用这条：`MALL_DB_USERNAME=klaus MALL_DB_PASSWORD=klaus ./mvnw clean verify -Pcoverage-check`；
  - `jacoco-prepare-agent` 关掉了 `append`：它默认是 `true`，会让 `jacoco.exec` 跨构建累加，
    门槛被上一次构建的数据喂饱（实测"删掉单测"都不报），关掉之后不依赖 `clean` 也可信；
- CI（`.github/workflows/verify.yml`）用 MySQL service container 跑全量，包括真库测试与覆盖率门槛。

"哪些是样例替身、真实项目该怎么做"见 [README.md](README.md) 的样例替身清单。

## 六、修改架构的正确流程

1. 先改本文（目录树 / 决策表 / 分层规则）。
2. 同步更新 `ArchitectureTest`。
3. 再动代码。
4. `./mvnw verify` 必须全绿。
