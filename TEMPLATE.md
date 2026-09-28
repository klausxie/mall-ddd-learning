# 模板使用说明

这个仓库是 **AI + DDD 的工程骨架**：规范写在文档里、由工具强制、改完有一条命令可验证。
新项目从这里初始化，而不是从零开始搭脚手架。

## 一、初始化

```bash
# 在模板仓库点 "Use this template" 建新仓，或直接复制跟踪文件
git clone <模板仓库> order-service && cd order-service

# 一条命令改名（包名 / 坐标 / 配置前缀 / 环境变量前缀 / 技能目录）
./scripts/init.sh cn.acme order-service cn.acme.order

# 骨架自检：这一步必须绿，之后再写业务
./mvnw clean verify
```

`scripts/init.sh` 只处理 **git 跟踪的文件**，所以 `target/`、`application-local.yaml`（含真实凭证）
不会被带进新项目——和 GitHub "Use this template" 的行为一致。

可选参数：

| 参数 | 作用 |
|---|---|
| `[configPrefix]` | 配置前缀，默认取 artifactId 第一段（`order-service` → `order`） |
| `--yes` | 免确认，适合脚本化 |
| `--force` | 跳过"工作区必须干净"的检查 |
| `--reset-git` | 删掉 `.git` 重新 init（只 `git add`，不替你 commit） |

它**不会**替你：删样例业务代码、写业务代码、commit。

## 二、初始化后清单

按顺序确认，每条都能在 2 分钟内做完：

- [ ] `./mvnw clean verify` 绿（12 条架构规则 + 单测 + 格式化 + Checkstyle）
- [ ] `pom.xml`：artifactId / groupId 已是新项目；`java.version` 与团队 JDK 一致
- [ ] `docker-compose.yml`：库名、账号、端口是否符合团队习惯
- [ ] `src/main/resources/application.yaml`：默认数据源指向本地；**确认里面没有任何真实凭证**
- [ ] `src/main/resources/application-local.yaml.example`：环境变量名已随项目改名（不再是模板的旧前缀）
- [ ] `.github/workflows/verify.yml`：CI 用的库名/端口与 compose 一致
- [ ] `src/main/resources/db/migration/V1__init_schema.sql`：删掉用不上的示例表；后续变更新增 `V2__xxx.sql`，**不要改已执行过的版本**
- [ ] `.claude/skills/<prefix>-conventions/SKILL.md` 与 `CLAUDE.md` / `ARCHITECTURE.md`：把示例业务（用户/地址）替换成自己的领域语言
- [ ] **替换下面的"样例替身"**，否则会带着假实现上线

## 三、样例替身清单（必须替换）

| 能力 | 模板里的做法 | 真实项目 |
|---|---|---|
| 短信验证码 | `mall.captcha.fixed-code=123456` 固定码 | 生成随机码、调短信网关、Redis 存 5 分钟 |
| 鉴权 | `X-Operator-Id` 请求头写入 `Context` | 解析 token / session；无身份由过滤器直接拒绝 |
| 消息队列 | `ApplicationEventPublisher` 只打日志并把事件转给 Spring 事件总线；`AfterCommitEventPublisher` 保证**提交后**才发 | 投递真实 MQ，并保证幂等与重试（保留提交后发布的语义） |
| 积分 | `RegistrationPointsPolicy` 纯计算 + 事件 | 账户服务消费事件并落库 |
| 密码哈希 | JDK 自带 PBKDF2（零第三方加密依赖） | 可换 BCrypt / Argon2，只改 `infrastructure/security` 实现类 |

## 四、骨架 / 样例怎么分

| 类别 | 内容 | 新项目怎么办 |
|---|---|---|
| **骨架（保留）** | `common/`（错误码 / Response / Page / Spec / Violation）、分层结构、12 条 ArchUnit 规则、Checkstyle + Spotless、CI（MySQL service + 覆盖率门槛）、compose + schema、四份文档 | 全部保留 |
| **黄金切片（照抄）** | `user` 域：Controller → Cmd/QueryService → assembler → 实体/Validator/Spec/Policy → Mapper + XML → 真库测试 | 大多数项目都有"用户/账号"，可改名复用；不适合就先照它写自己的第一个域，再删掉它 |
| **待你新增** | 你自己的业务域（一个域 = 一个 `domain/<业务>` + `application/<业务>` + 入口 + 表 + 测试） | 照黄金切片的形状加；加第二个域时，`ArchitectureTest` 里的上下文边界规则会开始生效 |

## 五、配合 AI 的用法

关键认知：**AI 的产出质量取决于反馈信号，不取决于提示词多华丽**。
所以第一步是把 `./mvnw verify` 和 CI 跑通，让每次改动都有红/绿反馈，然后再让它写业务。

给 AI 的任务提示词建议固定成这个形状：

```
需求：<一句话描述用例>

按仓库规范落地（先读 CLAUDE.md / ARCHITECTURE.md，细节查 <prefix>-conventions 技能）：
1. 规则先写进 domain：实体不变量 / XxxValidator / XxxSpec / XxxPolicy，并补单测
2. 再补 application：request / response + assembler + Cmd|QueryService（只编排，不写规则）
3. 最后补 web Controller：只用 GET/POST、路径 camelCase、返回 Response<T> 和响应模型
4. 要落库就同时改 Mapper 接口 + XML + schema（三者必须一致）
5. 断言只能出现在 domain；响应不得直接返回领域实体；失败一律带业务错误码

改完跑 ./mvnw verify，必须绿。不要改 config/ 下的规则文件、不要摘 pom 里的检查插件、不要用 -Dskip 绕过。
```

三条纪律：

1. **一次一个纵切**：一个用例从 Controller 到 XML 一次做完，别让 AI 横着铺（"把三个域都建起来"最危险）。
2. **先红后绿**：先让它写下会失败的测试（Spec / Policy / 真库端到端），再写实现。
3. **真库测试当验收**：`-Pcoverage-check -Dspring.profiles.active=local` 通过才算完成，纯单测容易骗过自己。

## 六、常见坑

- `application-local.yaml` 已在 `.gitignore` 里，**不要把凭证写进 `application.yaml`**。
- 改了分层或新增文件种类，**同步更新 `ARCHITECTURE.md` 与 `ArchitectureTest`**，并按 `ARCHITECTURE.md` §六 的顺序（先文档、再规则、再代码）。
- `ArchitectureTest` 的包名从 `Application` 推导，不要改回硬编码——否则改名脚本要改几十处、必漏。
- 检查失败时**改代码，不要改规则**；确实要调整规则，先说明理由并同时更新规则文件与文档。
