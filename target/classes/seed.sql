-- ============================================================================
-- 演示数据（可选）。docker compose 首次启动会自动执行；也可手工灌：
--   mysql -h 127.0.0.1 -u klaus -p mall < src/main/resources/seed.sql
--
-- 说明：
--   1. 只造商品，不造用户——用户请走 POST /user/create 注册（那样会顺带演示送积分）；
--   2. 用固定 id + INSERT IGNORE，重复执行安全。
-- ============================================================================

INSERT IGNORE INTO `product` (`id`, `status`, `name`, `description`, `content`, `cover`, `price`, `inventory`)
VALUES (1, 'ON_SALE', '示例商品·机械键盘', '87 键，红轴', '演示用详情文本', 'https://example.com/cover/keyboard.png',
        29900, 20),
       (2, 'PENDING', '示例商品·显示器支架', '单屏气压式', '演示用详情文本', 'https://example.com/cover/stand.png',
        15900, 5),
       (3, 'OFF_SALE', '示例商品·鼠标垫', '900x400', '演示用详情文本', 'https://example.com/cover/mat.png', 3900, 100);
