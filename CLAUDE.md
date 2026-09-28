# mall 项目开发规范

Spring Boot 3.5 + MyBatis-Plus + JDK 17 的单体 Java 项目。

## 构建与验证

```bash
./mvnw verify                      # 完整校验：格式化检查 + Checkstyle + ArchUnit 测试（不需要数据库）
./mvnw spotless:apply              # 只做格式化（本地改完代码先跑这个）
./mvnw spring-boot:run             # 启动（默认 8080）
./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local   # CI 跑法：真库端到端 + 覆盖率门槛
```

数据库连接串从环境变量取；本地调试需要：

```bash
export MALL_DB_PASSWORD='<密码>'
export MALL_DB_USERNAME='klaus'    # 可选，默认 klaus
```

或把凭证写进 `src/main/resources/application-local.yaml`（已在 `.gitignore` 里，模板见
`application-local.yaml.example`），再用 `--spring.profiles.active=local` 启动 / 跑测试。
**不要把凭证写进 `application.yaml`。**

**一轮改动结束前 `./mvnw verify` 必须通过。**

## 代码风格：交给工具，不要手工调

- 格式化由 **Spotless + Eclipse formatter** 统一，配置在 `config/eclipse-formatter.xml`。
  缩进 4 空格、行宽 120、大括号 K&R、import 顺序按 IDEA 默认布局。
- **不要手工调整缩进、换行、import 顺序**，改完运行 `./mvnw spotless:apply`。
- 静态检查由 **Checkstyle** 负责，配置在 `config/checkstyle.xml`，
  只管命名、修饰符、通配符导入和下面那些"禁令"，**刻意不含任何格式规则**（避免和 Spotless 互相打架）。
- IntelliJ IDEA 对齐方式：
  `Settings → Editor → Code Style → Java → 齿轮 → Import Scheme → Eclipse XML Profile`，
  选 `config/eclipse-formatter.xml`，并打开 `Actions on Save` 的 Reformat code / Optimize imports。

## 接口约定（强制）

- **HTTP 方法只允许 GET 和 POST。** 禁止 PUT / PATCH / DELETE。
  查询用 GET；新增、修改、删除一律用 POST，用路径上的动词区分。
- **路径用 camelCase**，例如 `/user/create`、`/address/create`、`/address/page`。
  禁止 kebab-case、下划线、全小写拼接。
- **分页参数固定为 `curPage` / `pageSize`**（`curPage` 从 1 开始，`pageSize` 默认 10）。
  禁止 `pageNumber` / `pageNum` / `currentPage` / `pageIndex` / `limit` / `offset` 作为对外参数名。
  统一继承 `common/model/Pageable`，返回 `common/model/Page<T>`。

上面这几条已经落成 Checkstyle 规则，违反会直接让 `./mvnw verify` 失败。

## 架构约束

分层、目录职责、"新文件放哪里"见 **@ARCHITECTURE.md**。
其中"谁不许依赖谁"由 `ArchitectureTest` 强制，违反会让测试失败。

## 硬性禁令

- 禁止 `System.out` / `System.err`，用 SLF4J（`@Slf4j`）。
- 禁止 `e.printStackTrace()`，用日志并保留异常。
- 禁止 `@Autowired` 注入，统一用构造器注入（Lombok `@AllArgsConstructor`）。
- 禁止通配符导入 `import xxx.*`。
- 禁止 `new Date()`，用 `java.time`。

## 检查失败时怎么办

**改代码，不要改规则。** 具体来说：

- 不要编辑 `config/checkstyle.xml` 或 `config/eclipse-formatter.xml` 来让检查通过；
- 不要从 `pom.xml` 里移除 spotless / checkstyle / archunit；
- 不要用 `-Dspotless.check.skip=true`、`-Dcheckstyle.skip=true`、`-DskipTests` 绕过。

确实认为规则本身需要调整时，**先说明理由并征求确认**，然后同时更新规则文件和本文件。
