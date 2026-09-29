# AGENTS.md

给**任意** AI coding agent 的入口说明（不绑定具体工具）。规范细节以
[CLAUDE.md](CLAUDE.md) 与 [ARCHITECTURE.md](ARCHITECTURE.md) 为准，本文件只列"最容易踩的"。

## 改完必须跑

```bash
./mvnw verify
```

它包含格式化检查、Checkstyle、ArchUnit 架构规则和单元测试，几秒内出结果。

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
- 领域实体不要直接当接口响应体，响应模型放 `application/<业务>/{command,query}/response`

## 分层（由 ArchUnit 强制）

```
web ──► application ──► domain ──► common
                          ▲
              infrastructure
```

`domain` 不得反向依赖；`domain` + `application` 不得直接依赖 `infrastructure`；
`common` 不得依赖任何业务包。

## 真库测试

数据源配了密码就跑（`MALL_DB_PASSWORD` 环境变量，或 local profile 的 `application-local.yaml`，
二者等价）；没配则跳过并打印原因。它们都带 `@Transactional`，数据自动回滚，
但 **Flyway 的建表 / 迁移不回滚**，所以只能指向本地或你专属的库。
命令、两种配法与注意事项见 README「验证」。

## 提交

`Conventional Commits` + 中文描述：`feat:` / `fix:` / `docs:` / `test:` / `refactor:` / `chore:`，
**一次提交只做一件事**。详见 CLAUDE.md 的「提交约定」。

凭证只放 `application-local.yaml`（`.gitignore` 已覆盖，别用 `git add -f`）；
提交进仓库的凭证删不掉、只能换——`GuardrailsTest` 会守着这两条。
