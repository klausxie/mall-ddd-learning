# AGENTS.md

给**任意** AI coding agent 的入口说明（不绑定具体工具）。规范细节以
[CLAUDE.md](CLAUDE.md) 与 [ARCHITECTURE.md](ARCHITECTURE.md) 为准，本文件只列"最容易踩的"。

## 改完必须跑

```bash
./mvnw verify
```

它包含格式化检查、Checkstyle、ArchUnit 架构规则、89 个用例（含真库端到端）和 domain 覆盖率地板。

但**别每改一行就跑它**：18 秒里约 15 秒是固定开销（Maven 启动、Spring 上下文、容器启动），跟改动大小无关。
按这个节奏迭代：

| 什么时候 | 跑什么 | 实测 |
|---|---|---|
| 每改一版代码 | `./mvnw -B -o -q spotless:apply checkstyle:check test-compile` | ~2s |
| 验某个类的行为 | `./mvnw -B -o test -Dtest=AddressApiTest` | ~5-11s |
| 收尾（前两条替代不了它） | `./mvnw verify` | ~14-19s |

想看项目现状（体量 / 封顶占比 / 覆盖率 / 真库走哪一级）：`scripts/facts.sh`，0.5 秒、只读。
另外注意 **`test` 阶段也已经包含格式化与 Checkstyle**，所以格式问题在 2 秒那一步就会暴露。

## 不许绕过检查

- 不要为了通过检查去改 `config/checkstyle.xml`、`config/eclipse-formatter.xml`；
- 不要从 `pom.xml` 移除 spotless / checkstyle / archunit；
- 不要用 `-Dspotless.check.skip` / `-Dcheckstyle.skip` / `-DskipTests` 绕过。

上面三条有机器兜底：`GuardrailsTest`（跑在 `./mvnw verify` 里）会在规则文件、pom 的检查插件
或 CI 参数被改弱时直接失败——它对任何工具都生效，不依赖某个 harness 的钩子。

确实认为规则要调整，先说清理由并征求确认，再同时更新规则文件、`GuardrailsTest` 与文档。

## 硬性约束（违反会直接让 verify 失败）

- 格式化交给工具：改完跑 `./mvnw spotless:apply`，不要手工调缩进 / 换行 / import 顺序
- HTTP 只用 GET / POST；路径 camelCase；分页参数只有 `curPage` / `pageSize`
- 禁止 `System.out` / `System.err` / `e.printStackTrace()` / `@Autowired` 字段注入 / 通配符导入 / `new Date()`
- 断言（`Assert` / `Asserts`）只允许出现在 `domain`：`application` / `web` 只能调用 domain 的
  Validator、Spec 或实体方法，需要报错就抛 `ErrorCodeException`
- 领域实体不要直接当接口响应体，响应模型（record）放 `application/<业务>/{command,query}`，与 Service 同包

## 分层（由 ArchUnit 强制）

```
web ──► application ──► domain ──► common
                          ▲
              infrastructure
```

`domain` 不得反向依赖；`domain` + `application` 不得直接依赖 `infrastructure`；
`common` 不得依赖任何业务包。

## 真库测试

`./mvnw verify` 默认就跑真库用例：数据源三级自动置备（显式配置 → 本机 Docker 起 `mysql:8.0` → H2 兜底），
不配任何东西也能跑全量。它们都带 `@Transactional`，数据自动回滚，但 **Flyway 的建表 / 迁移不回滚**，
所以自己配的库只能指向本地或你专属的库。详见 README「验证」。

想更快：`TESTCONTAINERS_REUSE_ENABLE=true` 会复用已有容器（全量约 19s → 14s，单类约 11s → 6s）。
代价是容器常驻（`docker ps` 可见，`docker rm -f` 清掉），且改动已应用过的迁移会因 Flyway checksum 报错
——那本来就是禁止的。

## 少走弯路的坑（都真实踩过）

- **`git add` / `git rm` 多路径要当心**：只要有一个路径不存在，整条命令就**中止，且一个文件都没暂存**——
  "提交信息写了、内容却不在里面"就是这么来的；`git rm` 遇到有未提交改动的文件也会中止，要加 `-f`。
  提交前用 `git status --short` 核对暂存结果。
- **沙箱里加依赖**：Maven 需要写 `~/.m2`，被拒时报 `Operation not permitted`，别误判成"依赖不存在"，
  放宽一次权限即可。
- **同一件事只写一处文档**：规范条文在 CLAUDE.md、结构在 ARCHITECTURE.md。副本必然掉队——
  本仓库删掉的 harness 技能就是这么过期的。

## 提交

`Conventional Commits` + 中文描述：`feat:` / `fix:` / `docs:` / `test:` / `refactor:` / `chore:`，
**一次提交只做一件事**。详见 CLAUDE.md 的「提交约定」。

凭证只放 `application-local.yaml`（`.gitignore` 已覆盖，别用 `git add -f`）；
提交进仓库的凭证删不掉、只能换——`GuardrailsTest` 会守着这两条。
