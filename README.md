# mall

Spring Boot 3.5 + MyBatis-Plus + JDK 17 的单体示例工程，用来演示**AI 驱动开发**下的一套完整约束：
规范写在文档里、能被工具强制、改完有一条命令可以验证。

## 环境要求

- JDK 17
- MySQL 8（本地或容器均可）
- Maven 用仓库自带的 `./mvnw`，无需本机安装

## 快速开始

```bash
# 1. 起本地 MySQL（首次启动会自动执行 schema.sql + seed.sql）
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

# 商品：新增 → 上架 → 分页
curl -s -X POST localhost:8080/product/create -H 'Content-Type: application/json' \
  -d '{"name":"示例商品","price":1999,"inventory":10}'
curl -s -X POST localhost:8080/product/onSale -H 'Content-Type: application/json' -d '{"productId":1}'
curl -s 'localhost:8080/product/page?curPage=1&pageSize=10&keyword=示例'
```

## 验证

```bash
./mvnw verify      # 格式 + 规范 + 架构 + 单测，不需要数据库
```

真库测试默认**跳过**，需要显式给密码（它们都在事务里跑，结束自动回滚）。
连自己的库时要带上 `local` profile；这就是 CI 的跑法，顺便校验覆盖率门槛（行覆盖 ≥ 80%）：

```bash
MALL_DB_PASSWORD='<你的密码>' ./mvnw clean verify -Pcoverage-check -Dspring.profiles.active=local
# 覆盖率报告：target/site/jacoco/index.html
```

| 测试 | 覆盖 |
|---|---|
| `ArchitectureTest` | 分层、依赖方向、命名、断言归属等 12 条架构规则 |
| `MapperStatementsTest` | Mapper 接口方法与 XML statement 一一对应（不需要 DB） |
| `ApplicationContextTest` | Bean 装配 + Mapper XML 解析（不需要 DB） |
| 各 `*Test` 单测 | Spec / Policy / 密码哈希 / 分页 / 上下文 / 商品状态机 / 地址归属等 |
| `MapperSmokeTest`、`*ApiTest` | 真库 SQL 与 HTTP 端到端（`MALL_DB_PASSWORD` 打开） |

## 接口文档

启动后 OpenAPI 描述在 `GET /v3/api-docs`（YAML 版 `/v3/api-docs.yaml`）。
交互式 Swagger UI 暂时关闭：springdoc 2.8.x 推导出的 UI 资源路径在 Spring Framework 6.2 更严格的
路径解析下非法（`No more pattern data allowed after **`），会让上下文启动失败；升级 springdoc 后可打开。

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/create` | 注册，响应回显赠送积分 |
| GET | `/user/get` | 当前用户 |
| POST | `/address/create`、`/address/update`、`/address/remove` | 地址增删改 |
| GET | `/address/page` | 地址分页 |
| POST | `/product/create`、`/product/update`、`/product/remove`、`/product/onSale`、`/product/offSale` | 商品增删改与上下架 |
| GET | `/product/page` | 商品分页（`keyword` / `status` 过滤） |

约定：只用 GET/POST；路径 camelCase；分页参数固定 `curPage`（从 1 开始）/ `pageSize`（默认 10）；
统一响应 `{code, message, data}`，`code=0` 为成功，失败靠业务错误码区分（HTTP 状态恒为 200）。

## 规范入口

- [CLAUDE.md](CLAUDE.md)：怎么做、硬性禁令、构建与验证
- [ARCHITECTURE.md](ARCHITECTURE.md)：目录职责、分层规则、已知偏差
- [.claude/skills/mall-conventions/SKILL.md](.claude/skills/mall-conventions/SKILL.md)：动手时照着抄的细节与新建接口清单
- [AGENTS.md](AGENTS.md)：给任意 AI coding agent 的入口（与 harness 无关）

## 样例替身清单（都是有意为之，不是遗漏）

| 能力 | 样例里的做法 | 真实项目该怎么做 |
|---|---|---|
| 短信验证码 | `mall.captcha.fixed-code=123456` 固定码 | 生成随机码、调短信网关、Redis 存 5 分钟 |
| 鉴权 | `X-Operator-Id` 请求头写入 `Context` | 解析 token / session，失效身份由过滤器拒绝 |
| 消息队列 | `RocketmqEventPublisher` 只打日志 | 投递 RocketMQ，并保证幂等与重试 |
| 积分 | `RegistrationPointsPolicy` 纯计算 + 事件回显 | 积分账户服务消费事件、落库 |
| 密码哈希 | JDK 自带 PBKDF2（不引第三方加密库） | 可换 BCrypt / Argon2，只改 `infrastructure/security` 实现类 |

## 目录结构

见 [ARCHITECTURE.md](ARCHITECTURE.md) 的目录树与"新文件放哪里"决策表。
