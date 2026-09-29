# mall

Spring Boot 3.5 + MyBatis-Plus + JDK 17 的 **AI + DDD 工程模板**：规范写在文档里、由工具强制、
改完有一条命令可以验证。新项目从它初始化，用法见 **[TEMPLATE.md](TEMPLATE.md)**。

本仓库同时也是模板的自演示：把模板本身的规则、测试、CI 跑通，就是新项目该有的起点。

怎么用它，选一种（详表见 [TEMPLATE.md](TEMPLATE.md) §一）：

| 用法 | 一句话 |
|---|---|
| ① 当模板克隆（首选） | `git clone -b <tag> <模板仓库> order-service` 后跑 `./scripts/init.sh`——改名与配置由脚本逐字带过 |
| ② 当范例阅读 | `git clone` 到 `/tmp/ref`，让 AI **读文件**（入口 `AGENTS.md`），在你自己的仓库里照做 |
| ③ 只给 AI 一个链接 | 下策：它会凭记忆重写那些"不显眼但决定性"的检查文件，必须配 TEMPLATE.md §五 的数字化验收 |

## 环境要求

- JDK 17
- MySQL 8（自带 compose，或用你自己的库；本机有 Docker 会自动起容器，没有则降级 H2）
- Maven 用仓库自带的 `./mvnw`，无需本机安装

## 快速开始

```bash
# 1. 起本地 MySQL（只建空库；建表由应用启动时的 Flyway 完成）
docker compose up -d

# 2. 配置数据源（compose 里就是 klaus/klaus；不配则用 application.yaml 的 localhost 默认值）
export APP_DB_USERNAME=klaus
export APP_DB_PASSWORD=klaus

# 3. 启动（默认 8080）
./mvnw spring-boot:run
```

连自己的库（不想用 compose）时，复制 `src/main/resources/application-local.yaml.example`
为 `application-local.yaml` 填上真实凭证，然后加 `--spring.profiles.active=local` 启动。
`application-local.yaml` 已在 `.gitignore` 里，**不要把凭证写进 `application.yaml`**。

### 走一遍黄金路径

```bash
# 注册：验证码用 application.yaml 里的样例固定码 123456；31 岁 → 送 300 积分
curl -s -X POST localhost:8080/user/create -H 'Content-Type: application/json' \
  -d '{"mobile":"13900000001","captcha":"123456","password":"Passw0rd!","age":31}'
# {"code":0,"message":"ok","data":{"user":{"id":1,"mobile":"13900000001","age":31},"points":300}}

# 29 岁 → 200 积分（分档边界）
curl -s -X POST localhost:8080/user/create -H 'Content-Type: application/json' \
  -d '{"mobile":"13900000002","captcha":"123456","password":"Passw0rd!","age":29}'

# 登录换令牌：响应体里有 token（给 App / 开放 API），同时下发 HttpOnly Cookie（给浏览器 / 后台）
TOKEN=$(curl -s -c /tmp/cookies.txt -X POST localhost:8080/user/login -H 'Content-Type: application/json' \
  -d '{"mobile":"13900000001","password":"Passw0rd!"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 方式一：带 Authorization 头（App / 服务间调用）
curl -s -X POST localhost:8080/address/create -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"recipient":"张三","phone":"13900000000","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园 1 号"}'

# 方式二：让浏览器带 Cookie（JS 读不到令牌，也就偷不走）；-b 复用上一步的 cookie jar
curl -s -b /tmp/cookies.txt 'localhost:8080/address/page?curPage=1&pageSize=10'

# 退出登录：清掉令牌 Cookie
curl -s -b /tmp/cookies.txt -c /tmp/cookies.txt -X POST localhost:8080/user/logout

# 不带凭证 → “未登录”（HTTP 仍是 200，靠 body.code=40005 区分）
curl -s 'localhost:8080/address/page'
```

## 验证

```bash
./mvnw verify      # 格式 + 规范 + 架构 + 单测 + domain 覆盖率地板，不需要数据库
```

真库测试（`MapperSmokeTest`、`*ApiTest`）**默认就跑**：数据源按三级降级自动置备，不用手工配任何东西。

| 级别 | 条件 | 用哪个库 |
|---|---|---|
| ① | 你配了数据源 | 你的库：`APP_DB_*` / `SPRING_DATASOURCE_*` 环境变量，或 `application-<profile>.yaml` 里的密码 |
| ② | 没配，但本机 Docker 可用 | 自动起一个 `mysql:8.0` 容器（Testcontainers，跑完由 Ryuk 回收；首次会拉镜像，约 600MB） |
| ③ | 没配，也没有 Docker | H2 的 MySQL 兼容模式快速通道（同一份 Flyway 迁移；日志会 WARN 说明引擎不是 MySQL） |

```bash
./mvnw verify                          # 默认：三级自动置备，跑全部 89 个用例
./mvnw clean verify -Pcoverage-check   # 再加"全局行覆盖 ≥ 80%"的门槛
# 想连你自己的库（只指向本地或你专属的库——Flyway 会在它上面建表 / 迁移）：
APP_DB_USERNAME=klaus APP_DB_PASSWORD='<密码>' ./mvnw clean verify -Pcoverage-check
./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local
# 覆盖率报告：target/site/jacoco/index.html
```

数据在事务里跑完自动回滚，但 **Flyway 的建表 / 迁移不回滚**。CI 用 service container（①）；
③ 只是本地兜底，**真 MySQL 始终由 CI 校验**。

想更快：`TESTCONTAINERS_REUSE_ENABLE=true` 复用上一次的容器（全量约 19s → 14s，单类约 11s → 6s）。
代价是容器常驻（`docker ps` 里能看到，`docker rm -f` 清掉），以及改动已应用过的迁移会因 Flyway checksum 报错。

项目现状（体量 / 封顶占比 / 覆盖率 / 真库走哪一级）一条命令：`scripts/facts.sh`（`--timings` 还能实测三条验证通道）。

| 测试 | 覆盖 |
|---|---|
| `ArchitectureTest` | 分层、依赖方向、命名、断言归属等 12 条架构规则 |
| `GuardrailsTest` | 护栏自检：真的跑一遍 Checkstyle 确认规则会报错，外加 pom / CI 参数没被改弱（不需要 DB） |
| `MapperStatementsTest` | Mapper 接口方法与 XML statement 一一对应（不需要 DB） |
| `EntityMappingTest` | 实体字段 ↔ resultMap / INSERT / UPDATE ↔ 建表列 对账（不需要 DB） |
| `ApplicationContextTest` | Bean 装配 + Mapper XML 解析（不需要 DB） |
| 各 `*Test` 单测 | Spec / Policy / 密码哈希 / 令牌签名 / 令牌 Cookie 属性 / 分页 / 上下文 / 地址归属等 |
| `MapperSmokeTest`、`*ApiTest` | 真库 SQL 与 HTTP 端到端（登录换令牌、Bearer 与 Cookie 两条路、地址增删改查；默认就跑：显式配置 → Docker 容器 → H2 兜底） |

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/create` | 注册，响应回显赠送积分 |
| POST | `/user/login` | 登录，返回令牌并下发 HttpOnly Cookie（手机号 + 密码） |
| POST | `/user/logout` | 退出，清掉令牌 Cookie |
| GET | `/user/get` | 当前用户（需要身份） |
| POST | `/address/create`、`/address/update`、`/address/remove` | 地址增删改（需要身份） |
| GET | `/address/page` | 地址分页（需要身份） |

约定：只用 GET/POST；路径 camelCase；分页参数固定 `curPage`（从 1 开始）/ `pageSize`（默认 10）；
需要身份的接口带两种凭证之一（同时带时**以 `Authorization` 为准**）：`Authorization: Bearer <token>`（App /
服务间调用）或登录下发的 HttpOnly Cookie（浏览器）；两者都没有或都非法则返回 `code=40005` 未登录；
统一响应 `{code, message, data}`，`code=0` 为成功，失败靠业务错误码区分（HTTP 状态恒为 200）。

## 鉴权

- **令牌**：`POST /user/login` 用手机号 + 密码换取 HMAC-SHA256 自签令牌（有效期 `app.auth.token-ttl`，
  默认 7 天），签名密钥来自 `app.auth.token-secret`——默认值只是样例替身，生产必须注入
  `APP_AUTH_TOKEN_SECRET`（未配置则登录直接失败）。配置项一律挂在固定的 `app.*` 前缀下，不随项目改名。
- **两种载体**：`Authorization: Bearer <token>` 给 App / 开放 API；HttpOnly Cookie 给浏览器 / 后台
  （`HttpOnly` 让 JS 读不到、`SameSite=Lax` 挡住跨站 POST、`Max-Age` 与令牌 TTL 一致）。HTTPS 环境把
  `app.auth.cookie-secure` 打开，否则浏览器不会回传。Cookie 名默认 `app_token`（`app.auth.cookie-name`）
  ——Cookie 不区分端口，同域下跑多个项目时改成各自的名字，免得互相顶掉。实现见
  `web/auth/OperatorCredentialResolver` 的两个实现类，要再加载体（比如 session id）加一个实现即可，
  `domain` / `application` 不用动。
- **CSRF**：`SameSite=Lax` 已让跨站 POST 不带 Cookie，而本仓库写操作一律 POST，所以没再引 CSRF 令牌；
  后台与其它业务**共享子域**（同 site、不同 origin）时 Lax 挡不住，那时要加 double-submit CSRF token。
- **吊销**：自签令牌签发后在过期前**无法作废**——`/user/logout` 只是让浏览器丢掉 Cookie。需要"强制下线 /
  改密即失效 / 单点登录"时必须改成服务端会话（Redis 或会话表），把 `TokenCodec` 换掉即可。

## 规范入口

- [TEMPLATE.md](TEMPLATE.md)：**怎么用它初始化新项目**、初始化后清单、AI 协作提示词模板
- [CLAUDE.md](CLAUDE.md)：怎么做、硬性禁令、构建与验证
- [ARCHITECTURE.md](ARCHITECTURE.md)：目录职责、分层规则、已知偏差、**新建接口要动哪几处**
- [AGENTS.md](AGENTS.md)：给任意 AI coding agent 的入口（与 harness 无关）
- [scripts/init.sh](scripts/init.sh)：一条命令改名初始化

## 样例替身清单

验证码、鉴权（登录令牌）、消息队列、积分、密码哈希这几处都是**有意为之的替身**（不是遗漏），
它们在 [TEMPLATE.md](TEMPLATE.md) 的"样例替身清单"里逐条列了真实项目该怎么替换。

## 目录结构

见 [ARCHITECTURE.md](ARCHITECTURE.md) 的目录树与"新文件放哪里"决策表。
