# mall

Spring Boot 3.5 + MyBatis-Plus + JDK 17 的 **AI + DDD 工程模板**：规范写在文档里、由工具强制、
改完有一条命令可以验证。新项目从它初始化，用法见 **[TEMPLATE.md](TEMPLATE.md)**。

本仓库同时也是模板的自演示：把模板本身的规则、测试、CI 跑通，就是新项目该有的起点。

## 环境要求

- JDK 17
- MySQL 8（本地或容器均可）
- Maven 用仓库自带的 `./mvnw`，无需本机安装

## 快速开始

```bash
# 1. 起本地 MySQL（只建空库；建表由应用启动时的 Flyway 完成）
docker compose up -d

# 2. 配置数据源（compose 里就是 klaus/klaus；不配则用 application.yaml 的 localhost 默认值）
export MALL_DB_USERNAME=klaus
export MALL_DB_PASSWORD=klaus

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

# 地址接口需要身份：样例用请求头 X-Operator-Id（值就是上一步返回的 user.id）
curl -s -X POST localhost:8080/address/create -H 'X-Operator-Id: 1' -H 'Content-Type: application/json' \
  -d '{"recipient":"张三","phone":"13900000000","province":"广东省","city":"深圳市","district":"南山区","detail":"科技园 1 号"}'
curl -s 'localhost:8080/address/page?curPage=1&pageSize=10' -H 'X-Operator-Id: 1'
```

## 验证

```bash
./mvnw verify      # 格式 + 规范 + 架构 + 单测 + domain 覆盖率地板，不需要数据库
```

真库测试的开关**只有一个**：数据源配了密码就跑，没配就跳过（跳过时控制台会打印原因，不会静默）。
所以下面两种配法等价，任选其一。数据在事务里跑完自动回滚，但 **Flyway 的建表 / 迁移不回滚**：

```bash
# ① 环境变量（CI 用的就是这种；也是 docker compose 那套账号）
MALL_DB_USERNAME=klaus MALL_DB_PASSWORD=klaus ./mvnw clean verify -Pcoverage-check
# ② 凭证写在 application-local.yaml，用 local profile 载入
./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local
# 覆盖率报告：target/site/jacoco/index.html
```

`-Pcoverage-check` 打开行覆盖 ≥ 80% 的门槛，它跟真库测试一起跑，所以上面两条命令都需要能连上库。
连自己的库时**只指向本地或你专属的库**——Flyway 会在那个库上建表 / 迁移。

没有本地库时不必强求：默认的 `./mvnw verify` 不需要数据库，已覆盖格式化、Checkstyle、ArchUnit、单测与 domain 覆盖率地板。

| 测试 | 覆盖 |
|---|---|
| `ArchitectureTest` | 分层、依赖方向、命名、断言归属等 12 条架构规则 |
| `GuardrailsTest` | 护栏自检：真的跑一遍 Checkstyle 确认规则会报错，外加 pom / CI 参数没被改弱（不需要 DB） |
| `MapperStatementsTest` | Mapper 接口方法与 XML statement 一一对应（不需要 DB） |
| `EntityMappingTest` | 实体字段 ↔ resultMap / INSERT / UPDATE ↔ 建表列 对账（不需要 DB） |
| `ApplicationContextTest` | Bean 装配 + Mapper XML 解析（不需要 DB） |
| 各 `*Test` 单测 | Spec / Policy / 密码哈希 / 分页 / 上下文 / 地址归属等 |
| `MapperSmokeTest`、`*ApiTest` | 真库 SQL 与 HTTP 端到端（数据源配了密码才跑，跳过会说明原因） |

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/create` | 注册，响应回显赠送积分 |
| GET | `/user/get` | 当前用户 |
| POST | `/address/create`、`/address/update`、`/address/remove` | 地址增删改 |
| GET | `/address/page` | 地址分页 |

约定：只用 GET/POST；路径 camelCase；分页参数固定 `curPage`（从 1 开始）/ `pageSize`（默认 10）；
统一响应 `{code, message, data}`，`code=0` 为成功，失败靠业务错误码区分（HTTP 状态恒为 200）。

## 规范入口

- [TEMPLATE.md](TEMPLATE.md)：**怎么用它初始化新项目**、初始化后清单、AI 协作提示词模板
- [CLAUDE.md](CLAUDE.md)：怎么做、硬性禁令、构建与验证
- [ARCHITECTURE.md](ARCHITECTURE.md)：目录职责、分层规则、已知偏差、**新建接口要动哪几处**
- [AGENTS.md](AGENTS.md)：给任意 AI coding agent 的入口（与 harness 无关）
- [scripts/init.sh](scripts/init.sh)：一条命令改名初始化

## 样例替身清单

验证码、鉴权、消息队列、积分、密码哈希这几处都是**有意为之的替身**（不是遗漏），
它们在 [TEMPLATE.md](TEMPLATE.md) 的"样例替身清单"里逐条列了真实项目该怎么替换。

## 目录结构

见 [ARCHITECTURE.md](ARCHITECTURE.md) 的目录树与"新文件放哪里"决策表。
