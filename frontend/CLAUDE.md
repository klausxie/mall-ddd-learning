# frontend 开发规范（React + Vite + TypeScript）

本目录是 mall 的前端。规则由工具强制，**改完必须跑一条命令**，不接受"我本地看没问题"。

## 命令

| 目的                                       | 命令                                | 实测 |
| ------------------------------------------ | ----------------------------------- | ---- |
| 起开发服（:5173，`/api` 代理到后端 :8080） | `pnpm dev`                          | ——   |
| 迭代时（改一行就跑）                       | `pnpm typecheck && pnpm lint`       | ~3s  |
| 跑测试                                     | `pnpm test --run`                   | ~1s  |
| **收尾（必须）**                           | `pnpm run verify`                   | ~5s  |
| 只格式化 / 只查格式                        | `pnpm format` / `pnpm format:check` | ~1s  |

`pnpm run verify` = `format:check → lint → typecheck → test --run → build`，是 `make verify-frontend`
与 CI 调用的同一条路径；**任何改动在收尾时都必须让它全绿**。构建产物在 `dist/`。

## 目录职责

- `src/api/` —— **唯一**的 HTTP 出口。`paths.ts` 是接口路径的**唯一**清单；
  `client.ts` 是唯一直接调用 `fetch` 的地方；`user.ts` / `auth.ts` / `address.ts` 按域封装。
- `src/pages/` —— 路由页面，只做"取数 + 编排"，请求一律经 api 层。
- `src/components/` —— 展示组件，只吃 props，不发请求。
- `src/__tests__/` —— 测试（含 `guardrails.test.ts`）。`src/styles.css` 是唯一的样式文件。

## 硬性规则

- **api 层是唯一 HTTP 出口**：`src/` 下除 `src/api/` 外不许出现 `fetch`，也不许出现字面量路径
  （如 `'/user/create'`）——路径只能引用 `API_PATHS`。这两条有测试机械强制。
- **不许跨目录 import 后端源码**：唯一契约是 HTTP。改路径 = 同时改后端 Controller 与 `paths.ts`，
  两边不一致会被后端 `ApiContractTest` 在构建期拦下。
- **令牌是 HttpOnly + SameSite=Lax 的 Cookie**：前端不存 token、不读 token，只靠 client.ts 的
  `credentials: 'include'`。开发态必须走 Vite 代理（`/api` → `127.0.0.1:8080` 并去掉前缀），
  **不要引入 CORS、不要给后端加 `context-path`**。
- **依赖克制**：不引 UI 组件库、axios、状态管理库、CSS 框架。HTTP 用 `fetch`，路由用 `react-router-dom`。
- **不许用 `eslint-disable`，不许 `any`**（护栏测试会扫源码）。错误统一用 `ApiError`（带 `code`/`message`）。
- 格式化与静态检查**交给工具**：不要手工调缩进 / 换行 / 引号，改完跑 `pnpm format`。

## 不许绕过检查

- 不要改 `eslint.config.js`、`prettier.config.js`、`tsconfig.json`、`vite.config.ts` 来让检查通过；
- 不要在 `package.json` 里删掉 `lint` / `typecheck` / `test` / `format:check` / `verify`，或加 `|| true` 之类；
- `src/__tests__/guardrails.test.ts` 会在 `pnpm run verify` 里校验以上两点（连 `tsconfig` 的严格项、
  ESLint 插件、Vite 代理、`.gitignore` 一起查）。确实认为规则要调整，先说明理由并征求确认，
  再同时更新规则文件、护栏测试与本文件。

## 接口约定（与后端一致）

HTTP 只用 GET / POST；路径 camelCase；分页参数固定 `curPage` / `pageSize`。
后端恒返回 `{code, message, data}`，`code === 0` 才是成功，其余（含非 2xx）一律抛 `ApiError`。
