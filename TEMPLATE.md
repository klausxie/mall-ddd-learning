# 模板使用说明

这个仓库是 **AI + 工程约束的工程骨架**，且**前后端同仓**：后端 Spring Boot 3.5 + MyBatis-Plus
（`backend/`，Maven），前端 React + Vite + TS（`frontend/`，pnpm）。规范写在文档里、由工具强制、
改完有一条命令可验证（`make verify`）。新项目从这里初始化，而不是从零开始搭脚手架。

## 一、初始化

### 三种用法（先选一种，别混着来）

| 用法 | 怎么做 | 什么时候用 |
|---|---|---|
| **① 当模板克隆**（首选） | `git clone -b <tag，如 v1-template> <模板仓库> order-service && cd order-service && ./scripts/init.sh cn.acme order-service cn.acme.order --yes --reset-git` | 自己起新项目。确定性最高：改名与配置由 `init.sh` 逐字带过 |
| **② 当范例阅读** | `git clone -b <同一 tag> <模板仓库> /tmp/ref`，让 AI **读 `/tmp/ref` 里的文件**（入口 `AGENTS.md`），在你自己的仓库里照做 | 要移植到别的语言 / 框架，或没有建仓权限 |
| **③ 只给 AI 一个链接** | 用 §五「5.1 从模板初始化」的提示词——它必须带逐字复制清单和数字化验收 | 前两种都不可行时的下策 |

为什么建议克隆而不是让 AI 照着链接重写：这个仓库的价值集中在**不显眼但决定性**的文件里——
后端 `backend/config/checkstyle.xml`、`ArchitectureTest`、`GuardrailsTest`、`backend/pom.xml` 里的门槛，
前端 `frontend/eslint.config.*`、`tsconfig*.json`、`vite.config.ts`。AI 凭记忆重写它们，得到的骨架
"看着像、牙齿没了"。另外务必**钉住 tag 而不是追 `main`**——AI 干活期间参照物不该漂移。

### 具体步骤

```bash
# 在模板仓库点 "Use this template" 建新仓，或直接克隆
git clone -b <tag，如 v1-template> <模板仓库> order-service && cd order-service

# 一条命令改名（后端包名 / 坐标 / 数据库名 + 前端项目名；配置前缀 app 与环境变量 APP_* 都固定，不改）
./scripts/init.sh cn.acme order-service cn.acme.order

# 骨架自检：这一步必须绿，之后再写业务
make verify
```

`scripts/init.sh` 只处理 **git 跟踪的文件**，所以 `backend/target/`、`frontend/node_modules/`、
`application-local.yaml`（含真实凭证）不会被带进新项目——和 GitHub "Use this template" 的行为一致。

它现在**同时改两端**：后端的包名（含源码目录）、`groupId` / `artifactId` / 数据库名，以及前端的
`frontend/package.json` 的 `name`（改成 `<artifactId>-frontend`）与 `frontend/index.html` 的标题。
配置项前缀 `app.*` 与环境变量 `APP_DB_*` / `APP_AUTH_*` 一律**固定不改**，所以代码与文档里的引用
不用跟着动；改完它会自检旧标识残留，漏了就报错而不是等编译时才发现。

可选参数：

| 参数 | 作用 |
|---|---|
| `[dbName]` | 数据库名（compose 建的库 + JDBC URL 里的库名），默认取 artifactId 第一段（`order-service` → `order`） |
| `--yes` | 免确认，适合脚本化 |
| `--force` | 跳过"工作区必须干净"的检查 |
| `--reset-git` | 删掉 `.git` 重新 init（只 `git add`，不替你 commit） |

它**不会**替你：删样例业务代码、写业务代码、commit。

## 二、初始化后清单

按顺序确认，每条都能在 2 分钟内做完：

- [ ] `make verify` 绿：后端（12 条架构规则 + 护栏自检 + 90 个用例 + domain 覆盖率地板 + 格式化 + Checkstyle）
      + 前端（format:check + lint + typecheck + test + build）
- [ ] `backend/pom.xml`：artifactId / groupId 已是新项目；`java.version` 与团队 JDK 一致
- [ ] `frontend/package.json`：`name` 已是新项目；Node 22 / pnpm 11 与团队一致
- [ ] `frontend/index.html` 标题已改；`frontend/vite.config.ts` 的开发端口（5173）与 `/api` 代理目标
      （后端 8080）符合你的部署——后端换端口这里必须同步
- [ ] `docker-compose.yml`（仓库根）：库名、账号、端口是否符合团队习惯
- [ ] `backend/src/main/resources/application.yaml`：默认数据源指向本地；**确认里面没有任何真实凭证**
- [ ] 鉴权：`APP_AUTH_TOKEN_SECRET` 已在部署环境注入；HTTPS 环境把 `app.auth.cookie-secure` 打开
      （配置里的 `local-dev-secret` 只是样例替身）
- [ ] `backend/src/main/resources/application-local.yaml.example`：环境变量名已随项目改名（不再是模板的旧前缀）
- [ ] `.github/workflows/verify.yml`：两个 job 用的库名/端口与 compose 一致
- [ ] `backend/src/main/resources/db/migration/V1__init_schema.sql`：删掉用不上的示例表；
      后续变更新增 `V2__xxx.sql`，**不要改已执行过的版本**
- [ ] `frontend/src/api/paths.ts` 与后端 Controller 一一对齐（`ApiContractTest` 在构建期对账）；
      示例页面（登录 / 地址）替换成自己的业务，不要留着占位
- [ ] `CLAUDE.md` / `ARCHITECTURE.md` / `backend/CLAUDE.md` / `frontend/CLAUDE.md`：把示例业务（用户/地址）
      替换成自己的领域语言（本模板不再内置 harness 技能）
- [ ] 真库测试的置备：`backend/src/test/java/**/support/RealDatabaseProvisioner.java` 默认三级
      （显式配置 → Docker 容器 → H2）。你若有 MySQL 专有 SQL，务必让 CI 用真库兜住（本模板 CI 已用 service container）
- [ ] **替换下面的"样例替身"**，否则会带着假实现上线

## 三、样例替身清单（必须替换）

| 能力 | 模板里的做法 | 真实项目 |
|---|---|---|
| 短信验证码 | `app.captcha.fixed-code=123456` 固定码 | 生成随机码、调短信网关、Redis 存 5 分钟 |
| 鉴权 | 登录签发 HMAC 自签令牌，同一份令牌两种载体（`Authorization: Bearer` / HttpOnly Cookie + `SameSite=Lax`）；签名密钥默认 `local-dev-secret` | 密钥从环境变量（`APP_AUTH_TOKEN_SECRET`）/ 密钥管理注入；HTTPS 打开 `app.auth.cookie-secure`；共享子域再加 CSRF 令牌；需要主动吊销就把令牌改存 Redis（见 README「鉴权」） |
| 消息队列 | `infrastructure/event/SpringEventPublisher` 只打日志并把事件转给 Spring 事件总线，`publishAfterCommit` 保证**提交后**才发 | 投递真实 MQ，并保证幂等与重试（保留提交后发布的语义） |
| 积分 | `RegistrationPointsPolicy` 纯计算 + 事件 | 账户服务消费事件并落库 |
| 密码哈希 | JDK 自带 PBKDF2（零第三方加密依赖） | 可换 BCrypt / Argon2，只改 `infrastructure/security` 实现类 |

## 四、骨架 / 样例怎么分

下表讲的都是**后端**（路径都在 `backend/` 下）；前端侧的骨架是 Vite + TS strict + ESLint/Prettier 配置、
`src/api/` 契约面与护栏测试，规则见 [frontend/CLAUDE.md](frontend/CLAUDE.md)。

| 类别 | 内容 | 新项目怎么办 |
|---|---|---|
| **骨架（保留）** | `common/`（错误码 / Response / Page / Spec / Violation）、分层结构、12 条 ArchUnit 规则、Checkstyle + Spotless、CI（MySQL service + 覆盖率门槛）、compose + schema、全套文档（根 + 各端 `CLAUDE.md`） | 全部保留 |
| **黄金切片（照抄）** | `user` 域：Controller → Cmd/QueryService → 实体/Validator/Spec/Policy → Mapper + XML → 真库测试 | 大多数项目都有"用户/账号"，可改名复用；不适合就先照它写自己的第一个域，再删掉它 |
| **待你新增** | 你自己的业务域（一个域 = 一个 `domain/<业务>` + `application/<业务>` + 入口 + 表 + 测试） | 照黄金切片的形状加；加第二个域时，`ArchitectureTest` 里的上下文边界规则会开始生效 |

## 五、配合 AI 的用法

关键认知：**AI 的产出质量取决于反馈信号，不取决于提示词多华丽**。
所以第一步是把 `make verify` 和 CI 跑通，让每次改动都有红/绿反馈，然后再让它写业务。

### 5.1 从模板初始化（如果这一步交给 AI）

**先说结论：优先交给 `scripts/init.sh`，不要交给 AI。** 初始化的每一步都是确定性的，而 AI 面对一个链接
只会抓几个文件、其余凭记忆重写，最容易丢掉那些"不显眼但决定性"的文件。真要交给它，就用这个提示词：

```
把 <模板仓库>@<tag 或 commit> 的骨架初始化到当前仓库（前后端同仓）。

逐字复制，不要重写、不要"优化"、不要省略注释——尤其这几个（AI 最容易擅自改写的）：
  backend/config/checkstyle.xml、backend/config/eclipse-formatter.xml
  backend/pom.xml（连同 4 个检查插件、两档覆盖率门槛、surefire 配置）
  backend/src/test/java/**/{ArchitectureTest,GuardrailsTest,ApiContractTest,EntityMappingTest,MapperStatementsTest}.java
  backend/src/test/java/**/support/**、backend/src/test/resources/**
  frontend/**（含 eslint.config.*、prettier.config.*（或 .prettierrc）、tsconfig*.json、vite.config.ts）
  backend/CLAUDE.md、frontend/CLAUDE.md
  .claude/**、scripts/**、.github/**
其余（backend/src/main、五份文档等）一律整仓复制，不要挑着抄。

然后：./scripts/init.sh <groupId> <artifactId> <basePackage> --yes --force
再按本文件「二、初始化后清单」「三、样例替身清单」替换样例业务。

验收（把命令与输出贴出来；任一条不满足就停下说明差异，不要"先继续、回头再补"）：
  1) make verify 全绿
  2) ./scripts/facts.sh 的封顶占比与覆盖率，与参照仓库相差 ≤ 1 个百分点
  3) git ls-files | wc -l 与参照仓库相差 ≤ 5
  4) 故意改坏一处（例如从 backend/config/checkstyle.xml 删掉一条规则），make verify 必须变红；
     前端同理：改坏 frontend/eslint.config.* 或 tsconfig 的 strict，make verify 也必须变红
```

第 4 条是整套验收里最重要的：它验证的不是"文件长得像"，而是**规则还生效**——两端都适用。

### 5.2 让它写业务（日常迭代）

给 AI 的任务提示词建议固定成这个形状：

```
需求：<一句话描述用例>

按仓库规范落地（先读 CLAUDE.md / backend/CLAUDE.md / frontend/CLAUDE.md，
新建接口要动哪几处见 ARCHITECTURE.md §二）：
1. 规则先写进 domain：实体不变量 / XxxValidator / XxxSpec / XxxPolicy，并补单测
2. 再补 application：XxxRequest / XxxInfo（record）+ Cmd|QueryService（只编排，不写规则）
3. 最后补 web Controller：只用 GET/POST、路径 camelCase、返回 Response<T> 和响应模型
4. 要落库就同时改 Mapper 接口 + XML + schema（三者必须一致）
5. 前端要调这个接口：路径加进 frontend/src/api/paths.ts，HTTP 调用只写在 frontend/src/api/ 下
6. 断言只能出现在 domain；响应不得直接返回领域实体；失败一律带业务错误码

改完跑 make verify，必须绿。不要改 backend/config/ 或前端检查配置里的规则文件、
不要摘 pom / package.json 里的检查插件与脚本、不要用 -Dskip 或 `|| true` 绕过。
```

三条纪律：

1. **一次一个纵切**：一个用例从 Controller 到 XML 一次做完，别让 AI 横着铺（"把三个域都建起来"最危险）。
2. **先红后绿**：先让它写下会失败的测试（Spec / Policy / 真库端到端），再写实现。
3. **真库测试当验收**：`make verify-backend`（含 `-Pcoverage-check`）通过才算完成，纯单测容易骗过自己。

节奏：改一版先跑该端的 2 秒快速通道，验行为只跑相关的测试类，收尾才 `make verify`——别每轮都跑全量：

| 什么时候 | 跑什么 | 实测 |
|---|---|---|
| 改后端一行 | `cd backend && ./mvnw -B -o -q spotless:apply checkstyle:check test-compile` | ~2s |
| 改前端一行 | `cd frontend && pnpm typecheck && pnpm lint` | ~2s |
| **收尾** | `make verify` | 两端全量 |

两端完整命令与实测耗时见 CLAUDE.md 的「验证节奏」。

## 六、常见坑

- `application-local.yaml` 已在 `.gitignore` 里，**不要把凭证写进 `application.yaml`**。
- 凭证**一旦提交进仓库就删不掉**（它留在 git 历史里）：只能先换凭证，再谈清历史。
  所以本地凭证只放 `backend/src/main/resources/application-local.yaml`、主配置只放 `${APP_DB_PASSWORD:}` 占位符——
  这两条由 `GuardrailsTest` 守着。
- **跨端路径与钩子/护栏里的路径必须同步改，否则护栏静默失效**：接口路径改了要同时动后端 Controller 与
  `frontend/src/api/paths.ts`（`ApiContractTest` 会拦）；目录或文件位置改了，`.claude/hooks/` 的正则、
  两端护栏测试里的路径字符串也要一起改——后端搬进 `backend/` 时就漏过一次，护栏看着还在、实际一次都没拦。
  改完务必跑一次 `make verify` 并确认它真的会拦（照 5.1 的第 4 条故意改坏一处试一次）。
- 改了分层或新增文件种类，**同步更新 `ARCHITECTURE.md` 与 `ArchitectureTest`**，并按 `ARCHITECTURE.md` §六
  的顺序（先文档、再规则、再代码）。
- `ArchitectureTest` 的包名从 `Application` 推导，不要改回硬编码——否则改名脚本要改几十处、必漏。
- 检查失败时**改代码，不要改规则**；确实要调整规则，先说明理由并同时更新规则文件、护栏测试与文档。
- 新项目若重写 `backend/config/checkstyle.xml`（或前端的 ESLint / tsconfig 严格度），护栏测试会红——
  这是**故意的**（防的就是"顺手把规则改弱"）。要么把新规则补进该测试的清单，要么按上一条流程改它。
