USE `xiao_ai_trade`;

-- Users Table
INSERT INTO `Users` (`username`, `password`, `role`, `email`, `personal_signature`) VALUES
('admin', 'adminpass', 'admin', 'admin@example.com', '系统管理员'),
('张三', 'password123', 'user', 'zhangsan@example.com', '我喜欢购物'),
('李四', 'password456', 'user', 'lisi@example.com', '科技爱好者'),
('王五', 'password789', 'user', 'wangwu@example.com', '追求性价比');

-- Products Table
INSERT INTO `Products` (`name`, `description`, `price`, `stock_quantity`, `category`, `image_url`, `status`) VALUES
('联想小新Pro 16 2023款笔记本电脑', '高性能轻薄本，16英寸2.5K高刷屏，酷睿i5处理器，16GB内存，512GB固态硬盘', 5999.00, 50, '笔记本电脑', 'images/lenovo_xiaoxinpro16.jpg', '新品'),
('华为MateBook X Pro 2023款', '高端旗舰笔记本，3.1K触摸屏，13代酷睿i7，16GB内存，1TB固态硬盘', 10999.00, 30, '笔记本电脑', 'images/huawei_matebookxpro.jpg', '热销'),
('小米13 Ultra 智能手机', '徕卡光学全焦段，第二代骁龙8移动平台，专业影像套装', 6499.00, 100, '智能手机', 'images/xiaomi_13ultra.jpg', '上架'),
('苹果 MacBook Air M2芯片', '13.6英寸Liquid视网膜显示屏，8核中央处理器，8核图形处理器，8GB统一内存，256GB固态硬盘', 9499.00, 40, '笔记本电脑', 'images/macbook_air_m2.jpg', '上架'),
('戴尔XPS 13 Plus笔记本', '简约设计，性能强劲，13.4英寸OLED触控屏，13代酷睿i7，32GB内存，1TB固态硬盘', 12999.00, 20, '笔记本电脑', 'images/dell_xps13plus.jpg', '下架'),
('机械革命 极光Pro 游戏本', '12代酷睿i9, RTX 3070Ti显卡, 16英寸2.5K 165Hz电竞屏', 8999.00, 25, '游戏本', 'images/jixiege_jiguangpro.jpg', '热销');

-- Orders Table
-- Order 1 for 张三 (user_id=2)
INSERT INTO `Orders` (`user_id`, `total_amount`, `status`, `shipping_address`) VALUES
(2, (SELECT price FROM Products WHERE product_id=1) + (SELECT price FROM Products WHERE product_id=3), '已完成', '北京市朝阳区测试地址123号');
SET @order1_id = LAST_INSERT_ID();

-- OrderItems for Order 1
INSERT INTO `OrderItems` (`order_id`, `product_id`, `quantity`, `price_at_purchase`) VALUES
(@order1_id, 1, 1, (SELECT price FROM Products WHERE product_id=1)),
(@order1_id, 3, 1, (SELECT price FROM Products WHERE product_id=3));

-- Order 2 for 李四 (user_id=3)
INSERT INTO `Orders` (`user_id`, `total_amount`, `status`, `shipping_address`) VALUES
(3, (SELECT price FROM Products WHERE product_id=2), '待发货', '上海市浦东新区测试路456号');
SET @order2_id = LAST_INSERT_ID();

-- OrderItems for Order 2
INSERT INTO `OrderItems` (`order_id`, `product_id`, `quantity`, `price_at_purchase`) VALUES
(@order2_id, 2, 1, (SELECT price FROM Products WHERE product_id=2));

-- Cart Table
-- Cart for 王五 (user_id=4)
INSERT INTO `Cart` (`user_id`) VALUES (4);
SET @cart1_id = LAST_INSERT_ID();

-- CartItems for 王五's Cart
INSERT INTO `CartItems` (`cart_id`, `product_id`, `quantity`) VALUES
(@cart1_id, 4, 1),
(@cart1_id, 6, 2);

-- Cart for 张三 (user_id=2)
INSERT INTO `Cart` (`user_id`) VALUES (2);
SET @cart2_id = LAST_INSERT_ID();

-- CartItems for 张三's Cart
INSERT INTO `CartItems` (`cart_id`, `product_id`, `quantity`) VALUES
(@cart2_id, 5, 1);


-- Reviews Table
-- Review 1 by 张三 (user_id=2) for 联想小新Pro 16 (product_id=1)
INSERT INTO `Reviews` (`product_id`, `user_id`, `rating`, `comment_text`, `status`) VALUES
(1, 2, 5, '电脑性能很好，屏幕显示效果出色，非常满意！', '已批准');

-- Review 2 by 李四 (user_id=3) for 华为MateBook X Pro (product_id=2)
INSERT INTO `Reviews` (`product_id`, `user_id`, `comment_text`, `is_anonymous`, `status`) VALUES
(2, 3, '外观漂亮，轻薄便携，但价格有点贵。', TRUE, '待审核');

-- Messages Table
-- Message 1 by 王五 (user_id=4)
INSERT INTO `Messages` (`user_id`, `message_text`, `status`) VALUES
(4, '请问戴尔XPS 13 Plus笔记本什么时候有货？', '待审核');

-- Message 2 (anonymous guest)
INSERT INTO `Messages` (`user_id`, `name`, `email`, `message_text`, `status`) VALUES
(NULL, '访客刘', 'guest@example.com', '网站建议：希望增加更多外设产品。', '已批准');
