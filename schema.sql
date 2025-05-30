CREATE DATABASE IF NOT EXISTS `xiao_ai_trade`
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `xiao_ai_trade`;

-- Users Table
CREATE TABLE IF NOT EXISTS `Users` (
    `user_id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT 'e.g., admin, user',
    `email` VARCHAR(100) UNIQUE,
    `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `personal_signature` TEXT,
    `last_login` TIMESTAMP NULL
) COMMENT='用户表';

-- Products Table
CREATE TABLE IF NOT EXISTS `Products` (
    `product_id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10, 2) NOT NULL,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `category` VARCHAR(100) COMMENT '商品分类',
    `image_url` VARCHAR(500) COMMENT '商品图片路径',
    `status` VARCHAR(50) DEFAULT '上架' COMMENT 'e.g., 上架, 下架, 新品, 热销',
    `creation_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `views` INT DEFAULT 0 COMMENT '浏览量'
) COMMENT='商品表';

-- Orders Table
CREATE TABLE IF NOT EXISTS `Orders` (
    `order_id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `status` VARCHAR(50) NOT NULL COMMENT 'e.g., 待付款, 待发货, 已发货, 已完成, 已取消',
    `shipping_address` TEXT NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`user_id`)
) COMMENT='订单表';

-- OrderItems Table
CREATE TABLE IF NOT EXISTS `OrderItems` (
    `order_item_id` INT PRIMARY KEY AUTO_INCREMENT,
    `order_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `price_at_purchase` DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (`order_id`) REFERENCES `Orders`(`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `Products`(`product_id`)
) COMMENT='订单项目表';

-- Cart Table
CREATE TABLE IF NOT EXISTS `Cart` (
    `cart_id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL UNIQUE COMMENT 'Each user has one cart',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`user_id`)
) COMMENT='购物车表';

-- CartItems Table
CREATE TABLE IF NOT EXISTS `CartItems` (
    `cart_item_id` INT PRIMARY KEY AUTO_INCREMENT,
    `cart_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `added_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`cart_id`) REFERENCES `Cart`(`cart_id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `Products`(`product_id`),
    UNIQUE KEY `cart_product_unique` (`cart_id`, `product_id`) COMMENT 'Ensure a product appears only once per cart, quantity is updated instead'
) COMMENT='购物车项目表';

-- Reviews Table
CREATE TABLE IF NOT EXISTS `Reviews` (
    `review_id` INT PRIMARY KEY AUTO_INCREMENT,
    `product_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `rating` INT COMMENT '评分 (e.g., 1-5 stars), if applicable',
    `comment_text` TEXT NOT NULL COMMENT '评价文字',
    `image_url` VARCHAR(500) COMMENT '补充图片路径',
    `video_url` VARCHAR(500) COMMENT '补充视频路径',
    `is_anonymous` BOOLEAN DEFAULT FALSE COMMENT '是否匿名评价',
    `review_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(50) DEFAULT '待审核' COMMENT 'e.g., 待审核, 已批准, 已拒绝',
    FOREIGN KEY (`product_id`) REFERENCES `Products`(`product_id`),
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`user_id`)
) COMMENT='评论表';

-- Messages Table
CREATE TABLE IF NOT EXISTS `Messages` (
    `message_id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NULL COMMENT '留言用户ID, can be 0 or a specific guest ID if anonymous messages from non-users are allowed',
    `name` VARCHAR(100) COMMENT '留言人姓名 (if not logged in)',
    `email` VARCHAR(100) COMMENT '留言人邮箱 (if not logged in)',
    `message_text` TEXT NOT NULL COMMENT '留言内容',
    `message_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(50) DEFAULT '待审核' COMMENT 'e.g., 待审核, 已批准',
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`user_id`) ON DELETE SET NULL
) COMMENT='留言表';
