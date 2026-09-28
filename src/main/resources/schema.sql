-- ============================================================================
-- mall 样例库表结构（MySQL 8 / utf8mb4）
--
-- 执行方式（手工执行一次即可）：
--   mysql -h <host> -u <user> -p <database> < src/main/resources/schema.sql
--
-- 说明：
--   1. 只做 CREATE TABLE IF NOT EXISTS，不含任何 DROP / DELETE / TRUNCATE，重复执行安全；
--   2. Spring Boot 的 spring.sql.init.mode 默认是 embedded，MySQL 下不会自动执行本文件，
--      必须手工执行（H2 这类嵌入式库的测试会自动执行）；
--   3. 列严格对应 domain 模型字段，模型里没有的属性不加列；
--   4. 唯一键与领域校验一一对应，见各表 CHECK / UNIQUE 的注释。
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
