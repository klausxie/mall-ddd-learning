# mall 项目开发规范

Spring Boot 3.5 + MyBatis-Plus + JDK 17 的单体 Java 项目。

## 构建与验证

```bash
./mvnw verify                      # 完整校验：格式化 + Checkstyle + ArchUnit + 单测 + domain 覆盖率地板（不需要数据库）
./mvnw spotless:apply              # 只做格式化（本地改完代码先跑这个）
./mvnw spring-boot:run             # 启动（默认 8080）
# 真库端到端 + 覆盖率门槛（本地默认跳过真库测试，覆盖率会虚低）；CI 用同样的命令：
MALL_DB_USERNAME=klaus MALL_DB_PASSWORD=klaus ./mvnw clean verify -Pcoverage-check
```

覆盖率分两档：默认 `verify` 卡 **`domain` 每个包行覆盖 ≥ 70%**（不需要数据库）；
`-Pcoverage-check` 再卡**全局**行覆盖 ≥ 80%，带真库测试跑，CI 跑这条。
两份门槛的机制与踩过的坑（jacoco 的 `append`）见 **@ARCHITECTURE.md** §五。

数据库连接串从环境变量取；本地调试需要：

```bash
export MALL_DB_PASSWORD='<密码>'
export MALL_DB_USERNAME='klaus'    # 可选，默认 klaus
```

或把凭证写进 `src/main/resources/application-local.yaml`（已在 `.gitignore` 里，模板见
`application-local.yaml.example`），再用 `--spring.profiles.active=local` 启动 / 跑测试；
但**只指向本地或你专属的库**——Flyway 会在这个库上建表 / 迁移，且迁移不在事务回滚范围内。

真库测试的开关只有一个：数据源配了密码就跑，没配则跳过并打印原因。CI 用的形式是：

```bash
MALL_DB_USERNAME=klaus MALL_DB_PASSWORD='<密码>' ./mvnw clean verify -Pcoverage-check
```

凭证放在 `application-local.yaml` 时改加 `-Dspring.profiles.active=local`——两种配法等价，见 README「验证」。

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

上面这几条有 `GuardrailsTest` 兜底：它在 `./mvnw verify` 里校验规则文件、pom 里的检查插件和 CI
参数没被改弱，所以"改规则让检查通过"过不了 verify。

护栏本身也**封顶**：护栏类代码（`GuardrailsTest` / `EntityMappingTest` / 钩子 / 真库开关）总量不超过
主代码的 40%，要加一条新检查就先合并或删掉一条价值更低的。见 ARCHITECTURE.md §五。

确实认为规则本身需要调整时，**先说明理由并征求确认**，然后同时更新规则文件、`GuardrailsTest` 和本文件。

## 提交约定

提交信息用 **Conventional Commits**，描述写中文，一行说清做了什么：

```
feat: 新增优惠券领域与校验
fix: 修正 30 岁分档边界少送 100 积分
test: 补 Address.validate 缺失的单测
docs: 说明真库测试的两种等价配法
refactor: 把积分规则从 application 挪回 domain
chore: 升级 spring-boot 到 3.5.1
```

- 常用 type：`feat` / `fix` / `docs` / `test` / `refactor` / `chore`；破坏性变更写 `feat!:`；
- **一次提交只做一件事**：不要把"顺手重构"和新功能混在一个提交里（AI 尤其容易这么干，review 时最难看懂）；
- 需要正文时空一行再写，正文写**为什么**，不写"改了什么"——改了什么看 diff 就有；
- 这条目前**没有机器强制**（不引 commitlint），靠自觉与 review。

## 凭证与配置

- 凭证只放 `src/main/resources/application-local.yaml`（已在 `.gitignore` 里，**不要**用 `git add -f` 强加）；
  主配置里只允许 `${MALL_DB_PASSWORD:}` 这类占位符。
- 凭证**一旦提交进仓库就删不掉**——它留在 git 历史里。真发生了，处置顺序是：① 换凭证（必须）；② 再谈清历史。
  `GuardrailsTest` 会守住上面这两条。
