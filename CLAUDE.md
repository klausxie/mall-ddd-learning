# mall：前后端同仓的工程模板

后端 Spring Boot 3.5 + MyBatis-Plus（`backend/`，Maven），前端 React + Vite + TS（`frontend/`，pnpm），同仓。
规范写在文档里、由工具强制、改完有一条命令可验证。

## 仓库地图

| 位置 | 是什么 | 该端的规则 |
|---|---|---|
| `backend/` | Java 后端（Maven，`./mvnw`） | **backend/CLAUDE.md** |
| `frontend/` | React + Vite + TS 前端（pnpm） | **frontend/CLAUDE.md** |
| `scripts/` | 仓库级工具：`init.sh` / `facts.sh` / `dev.sh` / `verify-all.sh` | 各脚本头部注释 |
| `AGENTS.md` | 给任意 AI coding agent 的入口 | 本文件 + 两端规则 |
| `ARCHITECTURE.md` | 仓库布局、后端分层、跨端契约、度量机制 | —— |
| `TEMPLATE.md` | 怎么用它初始化新项目 | —— |

## 验证节奏（省时间的关键）

| 什么时候 | 跑什么 | 实测 |
|---|---|---|
| 改后端一行 | `cd backend && ./mvnw -B -o -q spotless:apply checkstyle:check test-compile` | ~2s |
| 验后端某个类 | `cd backend && ./mvnw -B -o test -Dtest=AddressApiTest` | ~5-11s |
| 改前端一行 | `cd frontend && pnpm typecheck && pnpm lint` | ~2s |
| **收尾（必须）** | `make verify`（= `scripts/verify-all.sh`，两端全量） | 后端 ~19s + 前端 ~20s |

项目现状（两端体量 / 封顶占比 / 覆盖率 / 真库走哪一级）：`scripts/facts.sh`，0.5 秒、只读。
两端各自的完整命令与门槛，见 `backend/CLAUDE.md` 与 `frontend/CLAUDE.md`。

## 跨端边界（硬性）

- **唯一契约是 HTTP**。前端不许以任何方式 import 后端源码；后端不感知前端的存在。
- 前端的 HTTP 调用**只能出现在 `frontend/src/api/`**，路径一律取自 `src/api/paths.ts`。
- 改接口路径 = 同时改后端 Controller 与 `paths.ts`；两边不一致会被 `ApiContractTest` 在**构建期**拦下。
- 令牌是 **HttpOnly + SameSite=Lax 的 Cookie**：开发态走 Vite 代理、生产走同源反代，**不要引入 CORS**。
- 后端**不**加 `server.servlet.context-path`；`/api` 前缀只存在于前端代理与生产反代里（由它们 rewrite 掉）。

## 不许绕过检查

两端各自有护栏测试（后端 `GuardrailsTest`、前端护栏测试），检查规则文件、构建配置与 CI 参数没被改弱——
"改规则让检查通过"过不了 `make verify`。各端的禁令清单见该端的 `CLAUDE.md`。

## 封顶（**按端分别算**）

- 后端：检查类护栏 ≤ 主代码 40%，测试基础设施 ≤ 15%；
- 前端：检查类护栏（配置 + 护栏测试）≤ 源码 40%，测试行数单列参考。

分母混在一起这条规则就没有意义了。当前数值与理由见 `ARCHITECTURE.md` §五 与 `scripts/facts.sh`。

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

## 凭证

凭证只放本地：`backend/src/main/resources/application-local.yaml`（已在 `.gitignore` 里，**不要**用 `git add -f` 强加）；
主配置里只允许 `${APP_DB_PASSWORD:}` 这类占位符。凭证**一旦提交进仓库就删不掉**，
处置顺序是：① 换凭证（必须）；② 再谈清历史。
