# AGENTS.md

给**任意** AI coding agent 的入口说明（不绑定具体工具）。这是**前后端同仓**的仓库：

| 位置 | 是什么 | 该端的规则 |
|---|---|---|
| `backend/` | Spring Boot 3.5 + MyBatis-Plus（Maven，`./mvnw`） | [backend/CLAUDE.md](backend/CLAUDE.md) |
| `frontend/` | React + Vite + TS（pnpm） | [frontend/CLAUDE.md](frontend/CLAUDE.md) |

跨端规则与验证节奏见 [CLAUDE.md](CLAUDE.md)；目录布局、后端分层、跨端契约见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 改完必须跑

```bash
make verify                 # 两端全量：后端 verify + 前端 format/lint/type/test/build
make verify-backend         # 只跑后端（含真库端到端与覆盖率门槛）
make verify-frontend        # 只跑前端
```

但**别每改一行就跑全量**：后端那 18 秒里约 15 秒是固定开销（Maven 启动、Spring 上下文、容器启动），
跟改动大小无关。按这个节奏迭代：

| 什么时候 | 跑什么 | 实测 |
|---|---|---|
| 改后端一行 | `cd backend && ./mvnw -B -o -q spotless:apply checkstyle:check test-compile` | ~2s |
| 验后端某个类 | `cd backend && ./mvnw -B -o test -Dtest=AddressApiTest` | ~5-11s |
| 改前端一行 | `cd frontend && pnpm typecheck && pnpm lint` | ~2s |
| 收尾（前三条替代不了它） | `make verify` | ~40s |

仓库现状（两端体量 / 封顶占比 / 覆盖率 / 真库走哪一级）：`scripts/facts.sh`，0.5 秒、只读。
另外注意 **后端的 `test` 阶段已经包含格式化与 Checkstyle**，所以格式问题在 2 秒那一步就会暴露。

## 不许绕过检查

- 不要为了通过检查去改规则文件：后端 `backend/config/{checkstyle,eclipse-formatter}.xml`；
  前端 `frontend/{eslint.config.*,.prettierrc*,tsconfig*.json,vite.config.ts,vitest.config.ts}`；
- 不要从 `backend/pom.xml` 移除 spotless / checkstyle / archunit，也不要在 `frontend/package.json` 里
  摘掉 `lint` / `typecheck` / `test` / `verify` / `format:check` 脚本；
- 不要用 `-Dspotless.check.skip` / `-Dcheckstyle.skip` / `-DskipTests` / `eslint ... || true` 之类绕过。

上面这些有三层兜底：两端各一个护栏测试（跑在 `make verify` 里，检查规则文件与构建配置没被改弱）、
`.claude/hooks/guard-rules.mjs`（编辑时直接拦）、CI 显式传 `-D...skip=false`。
确实认为规则要调整，**先说明理由并征求确认**，再同时更新规则文件、护栏测试与文档。

## 跨端边界（最容易犯的错）

- 前端的 HTTP 调用**只能**出现在 `frontend/src/api/`，路径一律取自 `src/api/paths.ts`；
- 改接口路径 = 同时改后端 Controller 与 `paths.ts`——不一致会被 `ApiContractTest` 在**构建期**拦下；
- **不要引入 CORS、不要给后端加 `context-path`**：令牌是 HttpOnly + SameSite=Lax 的 Cookie，
  dev 靠 Vite 代理、prod 靠同源反代（细节见 CLAUDE.md「跨端边界」）。

## 真库测试（后端）

`make verify-backend` 默认就跑真库用例：数据源三级自动置备（显式配置 → 本机 Docker 起 `mysql:8.0` → H2 兜底），
不配任何东西也能跑全量。它们都带 `@Transactional`，数据自动回滚，但 **Flyway 的建表 / 迁移不回滚**，
所以自己配的库只能指向本地或你专属的库。详见 README「验证」。

想更快：`TESTCONTAINERS_REUSE_ENABLE=true make verify-backend` 复用容器（省约 5 秒/轮）。

## 少走弯路的坑（都真实踩过）

- **`git add` / `git rm` 多路径要当心**：只要有一个路径不存在，整条命令就**中止，且一个文件都没暂存**——
  "提交信息写了、内容却不在里面"就是这么来的；`git rm` 遇到有未提交改动的文件也会中止，要加 `-f`。
  提交前用 `git status --short` 核对暂存结果。
- **沙箱里加依赖**：后端 Maven 要写 `~/.m2`、前端 `pnpm install` 要写 `frontend/node_modules`，
  被拒时报 `Operation not permitted`，别误判成"包不存在"。
- **同一件事只写一处文档**：后端规则在 `backend/CLAUDE.md`、前端在 `frontend/CLAUDE.md`、
  跨端在根 `CLAUDE.md`。副本必然掉队——本仓库删掉的 harness 技能就是这么过期的。
- **改目录就是改护栏**：护栏与钩子里的路径一旦对不上就会**静默失效**。后端搬进 `backend/` 时，
  钩子里 `^config/` 的正则就失效过一次（看着还在，其实一次都没拦）。改目录后务必跑相关测试并检查钩子正则。

## 提交

`Conventional Commits` + 中文描述：`feat` / `fix` / `docs` / `test` / `refactor` / `chore`，
**一次提交只做一件事**。详见 CLAUDE.md 的「提交约定」。

凭证只放 `backend/src/main/resources/application-local.yaml`（`.gitignore` 已覆盖，别用 `git add -f`）；
提交进仓库的凭证删不掉、只能换——护栏测试会守着这一条。
