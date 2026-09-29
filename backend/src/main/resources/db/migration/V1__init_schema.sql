-- ============================================================================
-- mall 样例库表结构（MySQL 8 / utf8mb4）
--
-- 执行方式：**不要手工执行**。应用启动时由 Flyway 自动迁移（配置见 application.yaml 的
-- spring.flyway；spring.sql.init.mode=never 已显式关掉 Spring 自带的 schema.sql/data.sql 初始化）。
-- 变更方式：**新增 V2__xxx.sql，不要修改本文件**——Flyway 会校验已执行迁移的 checksum。
--
-- 说明：
--   1. 只做 CREATE TABLE IF NOT EXISTS，不含任何 DROP / DELETE / TRUNCATE，重复执行安全；
--   2. 列严格对应 domain 模型字段，模型里没有的属性不加列；
--   3. 唯一键与领域校验一一对应，见各表 UNIQUE KEY 的注释。
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 用户：对应 cn.mklaus.app.domain.user.User
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `mobile`     VARCHAR(20)  NOT NULL COMMENT '手机号，登录账号（UserValidator 要求唯一）',
    `password`   VARCHAR(100) NOT NULL COMMENT '密码哈希，不存明文',
    `nickname`   VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `avatar`     VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `age`        INT                   DEFAULT NULL COMMENT '年龄；注册时必填，由 UserMustBeAdultSpec 保证',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_mobile` (`mobile`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户';

-- ---------------------------------------------------------------------------
-- 收货地址：对应 cn.mklaus.app.domain.user.Address
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `address`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址 ID',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户 ID',
    `recipient`  VARCHAR(50)  NOT NULL COMMENT '收件人（Address.validate 必填）',
    `phone`      VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `province`   VARCHAR(50)           DEFAULT NULL COMMENT '省',
    `city`       VARCHAR(50)           DEFAULT NULL COMMENT '市',
    `district`   VARCHAR(50)           DEFAULT NULL COMMENT '区/县',
    `detail`     VARCHAR(255)          DEFAULT NULL COMMENT '详细地址',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_address_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址';
