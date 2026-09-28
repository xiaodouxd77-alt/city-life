/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : city-reviews

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 22/07/2026 13:04:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ADMIN',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_admin
-- ----------------------------
INSERT INTO `tb_admin` VALUES (1, 'admin', 'admin_salt_secret_01@cf26d3d1fe3f84669cd01f6e83cd9c46', 'ADMIN', '2026-07-05 22:52:27', '2026-07-05 22:52:27');

-- ----------------------------
-- Table structure for tb_badge
-- ----------------------------
DROP TABLE IF EXISTS `tb_badge`;
CREATE TABLE `tb_badge`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '勋章名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '勋章描述',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '勋章图标URL',
  `icon_text` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '勋章文本图标',
  `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '条件类型：blog_count/like_count/fan_count/credits/sign_days',
  `condition_value` int NOT NULL COMMENT '条件阈值',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_badge
-- ----------------------------
INSERT INTO `tb_badge` VALUES (1, '探店新手', '发布第一篇笔记', NULL, '🌱', 'blog_count', 1, 1, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (2, '探店达人', '发布10篇笔记', NULL, '🏆', 'blog_count', 10, 2, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (3, '探店大师', '发布50篇笔记', NULL, '🎯', 'blog_count', 50, 3, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (4, '人气新星', '笔记累计获赞50', NULL, '⭐', 'like_count', 50, 4, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (5, '人气之王', '笔记累计获赞500', NULL, '👑', 'like_count', 500, 5, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (6, '社交达人', '粉丝数达到100', NULL, '💬', 'fan_count', 100, 6, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (7, '社交红人', '粉丝数达到1000', NULL, '📢', 'fan_count', 1000, 7, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (8, '签到达人', '累计签到7天', NULL, '📅', 'sign_days', 7, 8, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (9, '勤勉签到', '累计签到30天', NULL, '🗓️', 'sign_days', 30, 9, '2026-07-20 22:23:16');
INSERT INTO `tb_badge` VALUES (10, '积分富翁', '累计获得1000积分', NULL, '💰', 'credits', 1000, 10, '2026-07-20 22:23:16');

-- ----------------------------
-- Table structure for tb_blog
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint NOT NULL COMMENT '商户id',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '探店的照片，最多9张，多张以\",\"隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '探店的文字描述',
  `liked` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` int NULL DEFAULT 0 COMMENT '状态: 0-正常, 1-隐藏',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 96 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_blog
-- ----------------------------
INSERT INTO `tb_blog` VALUES (24, 1, 1, '三里屯这家火锅太绝了！', 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg', '川渝老灶火锅真的是我吃过最好吃的火锅！毛肚特别新鲜，七上八下刚刚好。锅底麻辣鲜香，越吃越过瘾。', 33, 2, '2026-07-05 20:35:03', '2026-07-06 00:07:58', 0);
INSERT INTO `tb_blog` VALUES (25, 1, 2, '冬天就要吃火锅', 'https://qcloud.dpfile.com/pc/IOf6VX3qaBgFXFVgp75w-KKJmWZjFc8GXDU8g9bQC6YGCpAmG00QbfT4vCCBj7njuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg,https://qcloud.dpfile.com/pc/MZTdRDqCZdbPDUO0Hk6lZENRKzpKRF7kavrkEI99OxqBZTzPfIxa5E33gBfGouhFuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '寒冷的冬天，约上三五好友来三里屯吃火锅简直是人生一大幸事。这家店的环境很好，服务也很周到。性价比超高！', 28, 1, '2026-07-05 20:35:03', '2026-07-06 00:07:58', 0);
INSERT INTO `tb_blog` VALUES (26, 3, 5, '性价比超高的京味家常菜', 'https://p0.meituan.net/biztone/694233_1619500156517.jpeg,https://img.meituan.net/msmerchant/876ca8983f7395556eda9ceb064e6bc51840883.png', '京韵家常菜真的是平价京味菜天花板！京酱肉丝咸甜适口，老北京炸酱面地道无比。人均才60多，太划算了。', 49, 4, '2026-07-05 20:35:03', '2026-07-22 12:14:14', 0);
INSERT INTO `tb_blog` VALUES (27, 5, 2, '服务好到没话说', 'https://p0.meituan.net/biztone/163160492_1624251899456.jpeg', '蜀香源老火锅的服务一如既往地好，等位的时候还有零食和饮料。推荐番茄锅底，鲜香浓郁。虾滑Q弹，毛肚新鲜。', 70, 5, '2026-07-05 20:35:03', '2026-07-21 12:40:22', 0);
INSERT INTO `tb_blog` VALUES (28, 4, 1, '浪漫西餐约会圣地', 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '左岸西餐厅环境超棒，适合情侣约会。牛排五分熟刚刚好，搭配红酒简直完美。甜点提拉米苏也很惊艳。', 55, 6, '2026-07-05 20:35:03', '2026-07-21 17:58:17', 0);
INSERT INTO `tb_blog` VALUES (29, 2, 5, '烤肉控必打卡', 'https://p0.meituan.net/bbia/c1870d570e73accbc9fee90b48faca41195272.jpg,https://p0.meituan.net/mogu/397e40c28fc87715b3d5435710a9f88d706914.jpg', '炙子烤肉·京味坊真的是灵魂美味！五花肉烤得滋滋作响，蘸上特制酱料，一口下去满满的幸福感。鸡翅和牛肉也超级好吃。', 39, 3, '2026-07-05 20:35:03', '2026-07-17 23:18:05', 0);
INSERT INTO `tb_blog` VALUES (30, 8, 2, '亮马桥这家日料太惊艳', 'https://qcloud.dpfile.com/pc/MZTdRDqCZdbPDUO0Hk6lZENRKzpKRF7kavrkEI99OxqBZTzPfIxa5E33gBfGouhFuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '江户前寿司的刺身特别新鲜，三文鱼厚切入口即化。天妇罗外酥里嫩，味增汤也很正宗。午市套餐性价比很高。', 23, 2, '2026-07-05 20:35:03', '2026-07-06 00:07:58', 0);
INSERT INTO `tb_blog` VALUES (31, 1, 4, '一周来三次的真爱火锅', 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg,https://qcloud.dpfile.com/pc/IOf6VX3qaBgFXFVgp75w-KKJmWZjFc8GXDU8g9bQC6YGCpAmG00QbfT4vCCBj7njuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '我已经一周来三次了！每次都有新发现。黄喉、鹅肠、郡肝都超级新鲜。独家蘸料配方绝了，香辣过瘾！', 19, 2, '2026-07-05 20:35:03', '2026-07-21 12:39:37', 0);
INSERT INTO `tb_blog` VALUES (92, 9, 1, '火锅店', '', '太好吃了', 0, 0, '2026-07-11 23:05:58', '2026-07-17 23:18:05', 0);
INSERT INTO `tb_blog` VALUES (93, 5, 1012, '555', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/5/15/eeb2eb96-8a47-4b0c-8976-668783031d39.jpg', '5555', 0, 0, '2026-07-15 14:51:01', '2026-07-17 23:18:05', 0);
INSERT INTO `tb_blog` VALUES (94, 7, 5, '55', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/5/4/31ca921c-3926-45d2-9aa8-7a67283856bc.jpg', '555', 0, NULL, '2026-07-20 23:02:12', '2026-07-20 23:02:12', 0);
INSERT INTO `tb_blog` VALUES (95, 2, 1, '55555555555', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/0/8/52faeec6-a80d-4182-9bcb-c975c8181f5a.jpg', '5555599999999999', 0, NULL, '2026-07-21 12:41:06', '2026-07-21 12:41:06', 0);

-- ----------------------------
-- Table structure for tb_blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `blog_id` bigint UNSIGNED NOT NULL COMMENT '探店id',
  `parent_id` bigint UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answer_id` bigint UNSIGNED NOT NULL COMMENT '回复的评论id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
  `liked` int UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_blog_comments
-- ----------------------------
INSERT INTO `tb_blog_comments` VALUES (2, 2, 24, 0, 0, '看着就好吃！这家在哪呀', 5, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (3, 1, 24, 0, 0, '回复可可：在三里屯，具体可以导航搜一下~', 3, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (4, 5, 25, 0, 0, '冬天吃火锅太幸福了吧', 8, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (5, 4, 31, 0, 0, '一周三次是真的牛', 2, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (6, 2, 28, 0, 0, '适合约会！mark了', 6, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (7, 5, 28, 0, 0, '人均多少呀', 1, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (8, 1, 28, 0, 0, '回复可爱多：人均290，性价比很高', 4, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (9, 1, 27, 0, 0, '蜀香源yyds', 12, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (10, 5, 27, 0, 0, '番茄锅底确实好吃', 7, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (11, 4, 27, 0, 0, '等位的时候还有免费小吃~', 9, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (12, 2, 26, 0, 0, '京韵家常菜真的便宜又好吃', 10, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (13, 4, 26, 0, 0, '京酱肉丝绝了', 6, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (14, 1, 29, 0, 0, '五花肉看起来太诱人了', 4, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (15, 1, 30, 0, 0, '朋友聚会好地方！', 3, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (16, 5, 30, 0, 0, '午市套餐多少钱', 2, 0, '2026-07-06 00:07:58', '2026-07-06 00:07:58');
INSERT INTO `tb_blog_comments` VALUES (17, 1, 29, 0, 0, '真的假的', NULL, 0, '2026-07-06 08:46:25', '2026-07-06 08:46:25');
INSERT INTO `tb_blog_comments` VALUES (24, 1012, 27, 0, 0, '蜀香源的服务真的没话说', 6, 0, '2026-07-01 10:00:00', '2026-07-06 10:01:09');
INSERT INTO `tb_blog_comments` VALUES (25, 1017, 27, 0, 0, '最爱他们的番茄锅底', 4, 0, '2026-07-01 11:00:00', '2026-07-06 10:01:09');
INSERT INTO `tb_blog_comments` VALUES (28, 1018, 26, 0, 0, '性价比确实无敌', 5, 0, '2026-07-01 09:00:00', '2026-07-06 10:01:09');
INSERT INTO `tb_blog_comments` VALUES (29, 1019, 29, 0, 0, '烤肉控冲冲冲！', 9, 0, '2026-06-25 13:00:00', '2026-07-06 10:01:09');
INSERT INTO `tb_blog_comments` VALUES (36, 1, 31, 0, 0, '111', NULL, 0, '2026-07-21 12:39:37', '2026-07-21 12:39:37');
INSERT INTO `tb_blog_comments` VALUES (39, 2, 28, 0, 0, '88888888888888', NULL, 0, '2026-07-21 16:51:46', '2026-07-21 16:51:46');
INSERT INTO `tb_blog_comments` VALUES (40, 2, 28, 0, 0, '999999999999999', NULL, 0, '2026-07-21 17:15:00', '2026-07-21 17:15:00');
INSERT INTO `tb_blog_comments` VALUES (41, 2, 28, 0, 0, '6666666666666666666', NULL, 0, '2026-07-21 17:17:52', '2026-07-21 17:17:52');
INSERT INTO `tb_blog_comments` VALUES (42, 1, 26, 0, 0, '9999999999999999999999', NULL, 0, '2026-07-22 12:13:09', '2026-07-22 12:13:09');

-- ----------------------------
-- Table structure for tb_credits_exchange
-- ----------------------------
DROP TABLE IF EXISTS `tb_credits_exchange`;
CREATE TABLE `tb_credits_exchange`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `voucher_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '兑换的优惠券ID',
  `credits_cost` int NOT NULL COMMENT '消耗积分',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0处理中/1成功/2失败',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_credits_exchange
-- ----------------------------
INSERT INTO `tb_credits_exchange` VALUES (1, 5, 1, 300, 1, '2026-07-20 22:58:39', '2026-07-20 22:58:39');
INSERT INTO `tb_credits_exchange` VALUES (2, 5, 10, 100, 1, '2026-07-21 00:01:27', '2026-07-21 00:01:27');
INSERT INTO `tb_credits_exchange` VALUES (3, 1, 66, 300, 1, '2026-07-22 12:46:29', '2026-07-22 12:46:29');

-- ----------------------------
-- Table structure for tb_credits_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_credits_log`;
CREATE TABLE `tb_credits_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` int NOT NULL COMMENT '变动积分（正增负减）',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型：sign/blog/like/comment_like/follow/login/exchange',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_type`(`user_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_credits_log
-- ----------------------------
INSERT INTO `tb_credits_log` VALUES (1, 5, 2, 'sign', 'daily sign-in +2', '2026-07-20 22:58:18');
INSERT INTO `tb_credits_log` VALUES (2, 5, -300, 'exchange', 'exchange voucher \"50元代金券\" cost 300 credits', '2026-07-20 22:58:39');
INSERT INTO `tb_credits_log` VALUES (3, 5, 5, 'blog', 'publish blog +5', '2026-07-20 23:02:12');
INSERT INTO `tb_credits_log` VALUES (4, 5, 1, 'follow', 'follow user +1', '2026-07-20 23:06:52');
INSERT INTO `tb_credits_log` VALUES (5, 5, 1, 'like', 'blog liked +1', '2026-07-20 23:08:36');
INSERT INTO `tb_credits_log` VALUES (6, 5, 2, 'sign', 'daily sign-in +2', '2026-07-21 00:00:34');
INSERT INTO `tb_credits_log` VALUES (7, 5, -100, 'exchange', 'exchange voucher \"30元代金券\" cost 100 credits', '2026-07-21 00:01:27');
INSERT INTO `tb_credits_log` VALUES (8, 5, 1, 'follow', 'follow user +1', '2026-07-21 00:50:48');
INSERT INTO `tb_credits_log` VALUES (9, 2, 1, 'login', 'first login today +1', '2026-07-21 09:10:05');
INSERT INTO `tb_credits_log` VALUES (10, 2, 2, 'sign', 'daily sign-in +2', '2026-07-21 09:12:27');
INSERT INTO `tb_credits_log` VALUES (11, 2, 1, 'like', 'blog liked +1', '2026-07-21 10:04:18');
INSERT INTO `tb_credits_log` VALUES (12, 2, 1, 'follow', 'follow user +1', '2026-07-21 10:15:20');
INSERT INTO `tb_credits_log` VALUES (13, 1, 1, 'login', 'first login today +1', '2026-07-21 12:39:03');
INSERT INTO `tb_credits_log` VALUES (14, 1, 2, 'sign', 'daily sign-in +2', '2026-07-21 12:39:10');
INSERT INTO `tb_credits_log` VALUES (15, 4, 1, 'like', 'blog liked +1', '2026-07-21 12:39:27');
INSERT INTO `tb_credits_log` VALUES (16, 2, 1, 'like', 'blog liked +1', '2026-07-21 12:40:22');
INSERT INTO `tb_credits_log` VALUES (17, 1, 5, 'blog', 'publish blog +5', '2026-07-21 12:41:07');
INSERT INTO `tb_credits_log` VALUES (18, 2, 1, 'follow', 'follow user +1', '2026-07-21 16:49:23');
INSERT INTO `tb_credits_log` VALUES (19, 1, 1, 'like', 'blog liked +1', '2026-07-21 16:49:39');
INSERT INTO `tb_credits_log` VALUES (20, 1, 1, 'follow', 'follow user +1', '2026-07-21 17:16:03');
INSERT INTO `tb_credits_log` VALUES (21, 1, 1, 'follow', 'follow user +1', '2026-07-21 17:16:27');
INSERT INTO `tb_credits_log` VALUES (22, 1, 1, 'like', 'blog liked +1', '2026-07-21 17:58:18');
INSERT INTO `tb_credits_log` VALUES (23, 1, 1, 'login', 'first login today +1', '2026-07-22 09:18:02');
INSERT INTO `tb_credits_log` VALUES (24, 5, 1, 'login', 'first login today +1', '2026-07-22 12:12:34');
INSERT INTO `tb_credits_log` VALUES (25, 5, 2, 'sign', 'daily sign-in +2', '2026-07-22 12:12:39');
INSERT INTO `tb_credits_log` VALUES (26, 1, 1, 'follow', 'follow user +1', '2026-07-22 12:13:29');
INSERT INTO `tb_credits_log` VALUES (27, 5, 1, 'like', 'blog liked +1', '2026-07-22 12:14:14');
INSERT INTO `tb_credits_log` VALUES (28, 1, 2, 'sign', 'daily sign-in +2', '2026-07-22 12:34:53');
INSERT INTO `tb_credits_log` VALUES (29, 1, -300, 'exchange', 'exchange voucher \"平台通用50元代金券\" cost 300 credits', '2026-07-22 12:46:29');

-- ----------------------------
-- Table structure for tb_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tb_favorite`;
CREATE TABLE `tb_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: SHOP/BLOG',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_type`(`user_id` ASC, `target_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_favorite
-- ----------------------------
INSERT INTO `tb_favorite` VALUES (3, 5, 'BLOG', 29, '2026-07-17 22:54:28');
INSERT INTO `tb_favorite` VALUES (9, 5, 'BLOG', 26, '2026-07-20 23:08:37');
INSERT INTO `tb_favorite` VALUES (10, 5, 'SHOP', 2, '2026-07-20 23:22:09');
INSERT INTO `tb_favorite` VALUES (11, 5, 'SHOP', 6, '2026-07-20 23:28:02');
INSERT INTO `tb_favorite` VALUES (12, 5, 'SHOP', 1, '2026-07-20 23:42:49');

-- ----------------------------
-- Table structure for tb_follow
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint UNSIGNED NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_follow
-- ----------------------------
INSERT INTO `tb_follow` VALUES (3, 1, 4, '2026-07-05 20:34:26');
INSERT INTO `tb_follow` VALUES (5, 2, 5, '2026-07-05 20:34:26');
INSERT INTO `tb_follow` VALUES (8, 4, 1, '2026-07-05 20:34:26');
INSERT INTO `tb_follow` VALUES (9, 4, 2, '2026-07-05 20:34:26');
INSERT INTO `tb_follow` VALUES (10, 4, 5, '2026-07-05 20:34:26');
INSERT INTO `tb_follow` VALUES (41, 2, 4, '2026-07-06 10:01:09');
INSERT INTO `tb_follow` VALUES (50, 5, 4, '2026-07-06 10:01:09');
INSERT INTO `tb_follow` VALUES (77, 5, 2, '2026-07-11 14:41:28');
INSERT INTO `tb_follow` VALUES (79, 1012, 1, '2026-07-15 14:45:44');
INSERT INTO `tb_follow` VALUES (80, 1012, 2, '2026-07-15 14:45:47');
INSERT INTO `tb_follow` VALUES (81, 1012, 5, '2026-07-15 15:28:37');
INSERT INTO `tb_follow` VALUES (86, 5, 1, '2026-07-21 00:50:47');
INSERT INTO `tb_follow` VALUES (88, 2, 1, '2026-07-21 16:49:23');
INSERT INTO `tb_follow` VALUES (90, 1, 2, '2026-07-21 17:16:26');
INSERT INTO `tb_follow` VALUES (91, 1, 5, '2026-07-22 12:13:28');

-- ----------------------------
-- Table structure for tb_notification
-- ----------------------------
DROP TABLE IF EXISTS `tb_notification`;
CREATE TABLE `tb_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: LIKE/COMMENT/REPLY/FOLLOW/SYSTEM',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知内容',
  `target_id` bigint NULL DEFAULT NULL COMMENT '关联目标ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '触发通知的用户ID',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_notification
-- ----------------------------
INSERT INTO `tb_notification` VALUES (1, 2, 'FOLLOW', '可爱多 关注了你', NULL, 5, 1, '2026-07-11 14:41:28');
INSERT INTO `tb_notification` VALUES (2, 2, 'FOLLOW', '张梓涵 关注了你', NULL, 1012, 1, '2026-07-15 14:45:22');
INSERT INTO `tb_notification` VALUES (3, 1, 'FOLLOW', '张梓涵 关注了你', NULL, 1012, 1, '2026-07-15 14:45:45');
INSERT INTO `tb_notification` VALUES (4, 2, 'FOLLOW', '张梓涵 关注了你', NULL, 1012, 1, '2026-07-15 14:45:48');
INSERT INTO `tb_notification` VALUES (5, 5, 'FOLLOW', '张梓涵 关注了你', NULL, 1012, 1, '2026-07-15 15:28:37');
INSERT INTO `tb_notification` VALUES (6, 2, 'LIKE', '可爱多 赞了你的笔记', 27, 5, 1, '2026-07-18 10:18:53');
INSERT INTO `tb_notification` VALUES (7, 1012, 'FOLLOW', '可爱多 关注了你', NULL, 5, 0, '2026-07-19 12:51:49');
INSERT INTO `tb_notification` VALUES (8, 1012, 'FOLLOW', '可爱多 关注了你', NULL, 5, 0, '2026-07-19 15:26:33');
INSERT INTO `tb_notification` VALUES (9, 1012, 'FOLLOW', '可爱多 关注了你', NULL, 5, 0, '2026-07-20 11:52:29');
INSERT INTO `tb_notification` VALUES (10, 1012, 'FOLLOW', '可爱多 关注了你', NULL, 5, 0, '2026-07-20 23:06:52');
INSERT INTO `tb_notification` VALUES (11, 1, 'FOLLOW', '可爱多 关注了你', NULL, 5, 1, '2026-07-21 00:50:48');
INSERT INTO `tb_notification` VALUES (12, 1012, 'FOLLOW', '可可今天不吃肉 关注了你', NULL, 2, 0, '2026-07-21 10:15:20');
INSERT INTO `tb_notification` VALUES (13, 4, 'LIKE', '小鱼同学 赞了你的笔记', 31, 1, 0, '2026-07-21 12:39:27');
INSERT INTO `tb_notification` VALUES (14, 4, 'COMMENT', '小鱼同学 评论了你的笔记', 31, 1, 0, '2026-07-21 12:39:38');
INSERT INTO `tb_notification` VALUES (15, 2, 'LIKE', '小鱼同学 赞了你的笔记', 27, 1, 1, '2026-07-21 12:40:22');
INSERT INTO `tb_notification` VALUES (16, 1, 'FOLLOW', '可可今天不吃肉 关注了你', NULL, 2, 1, '2026-07-21 16:49:23');
INSERT INTO `tb_notification` VALUES (17, 1, 'LIKE', '可可今天不吃肉 赞了你的笔记', 28, 2, 1, '2026-07-21 16:49:39');
INSERT INTO `tb_notification` VALUES (18, 1, 'COMMENT', '可可今天不吃肉 评论了你的笔记', 28, 2, 1, '2026-07-21 16:50:00');
INSERT INTO `tb_notification` VALUES (19, 1, 'COMMENT', '可可今天不吃肉 评论了你的笔记', 28, 2, 1, '2026-07-21 16:50:24');
INSERT INTO `tb_notification` VALUES (20, 1, 'COMMENT', '可可今天不吃肉 评论了你的笔记', 28, 2, 1, '2026-07-21 16:51:47');
INSERT INTO `tb_notification` VALUES (21, 1, 'COMMENT', '可可今天不吃肉 评论了你的笔记', 28, 2, 1, '2026-07-21 17:15:00');
INSERT INTO `tb_notification` VALUES (22, 2, 'FOLLOW', '小鱼同学 关注了你', NULL, 1, 1, '2026-07-21 17:16:03');
INSERT INTO `tb_notification` VALUES (23, 2, 'FOLLOW', '小鱼同学 关注了你', NULL, 1, 1, '2026-07-21 17:16:27');
INSERT INTO `tb_notification` VALUES (24, 1, 'COMMENT', '可可今天不吃肉 评论了你的笔记', 28, 2, 1, '2026-07-21 17:17:53');
INSERT INTO `tb_notification` VALUES (25, 5, 'COMMENT', '小鱼同学 评论了你的笔记', 26, 1, 0, '2026-07-22 12:13:09');
INSERT INTO `tb_notification` VALUES (26, 5, 'FOLLOW', '小鱼同学 关注了你', NULL, 1, 0, '2026-07-22 12:13:29');
INSERT INTO `tb_notification` VALUES (27, 5, 'LIKE', '小鱼同学 赞了你的笔记', 26, 1, 0, '2026-07-22 12:14:14');

-- ----------------------------
-- Table structure for tb_rating
-- ----------------------------
DROP TABLE IF EXISTS `tb_rating`;
CREATE TABLE `tb_rating`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '评分用户ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `blog_id` bigint NULL DEFAULT NULL COMMENT '关联笔记ID',
  `score` int NOT NULL COMMENT '评分 1-5',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺评分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_rating
-- ----------------------------
INSERT INTO `tb_rating` VALUES (1, 1, 9, 92, 5, '2026-07-11 23:05:58', '2026-07-11 23:05:58');
INSERT INTO `tb_rating` VALUES (2, 1012, 5, 93, 4, '2026-07-15 14:51:02', '2026-07-15 14:51:02');
INSERT INTO `tb_rating` VALUES (3, 1, 2, 95, 5, '2026-07-21 12:41:07', '2026-07-21 12:41:07');

-- ----------------------------
-- Table structure for tb_seckill_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher`  (
  `voucher_id` bigint UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
  `stock` int NOT NULL COMMENT '库存',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `begin_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀优惠券表，与优惠券是一对一关系' ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_seckill_voucher
-- ----------------------------
INSERT INTO `tb_seckill_voucher` VALUES (60, 50, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');
INSERT INTO `tb_seckill_voucher` VALUES (61, 40, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');
INSERT INTO `tb_seckill_voucher` VALUES (62, 30, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');
INSERT INTO `tb_seckill_voucher` VALUES (63, 60, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');
INSERT INTO `tb_seckill_voucher` VALUES (64, 25, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');
INSERT INTO `tb_seckill_voucher` VALUES (65, 80, '2026-07-21 09:06:30', '2026-07-21 08:06:30', '2026-07-28 09:06:30', '2026-07-21 09:06:30');

-- ----------------------------
-- Table structure for tb_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺名称',
  `type_id` bigint UNSIGNED NOT NULL COMMENT '商铺类型的id',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺图片，多个图片以\',\'隔开',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商圈，例如陆家嘴',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
  `x` double UNSIGNED NOT NULL COMMENT '经度',
  `y` double UNSIGNED NOT NULL COMMENT '维度',
  `avg_price` bigint UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
  `sold` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '销量',
  `comments` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '评论数量',
  `score` int(2) UNSIGNED ZEROFILL NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间，例如 10:00-22:00',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_shop
-- ----------------------------
INSERT INTO `tb_shop` VALUES (1, '川渝老灶火锅（三里屯店）', 1, 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg,https://qcloud.dpfile.com/pc/IOf6VX3qaBgFXFVgp75w-KKJmWZjFc8GXDU8g9bQC6YGCpAmG00QbfT4vCCBj7njuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '三里屯', '朝阳区三里屯路19号太古里南区S3-31', 116.4538, 39.9332, 128, 0000004215, 0000000003, 37, '11:00-02:00', '2021-12-22 18:10:39', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (2, '炙子烤肉·京味坊（望京店）', 1, 'https://p0.meituan.net/bbia/c1870d570e73accbc9fee90b48faca41195272.jpg,http://p0.meituan.net/mogu/397e40c28fc87715b3d5435710a9f88d706914.jpg,https://qcloud.dpfile.com/pc/MZTdRDqCZdbPDUO0Hk6lZENRKzpKRF7kavrkEI99OxqBZTzPfIxa5E33gBfGouhFuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '望京', '朝阳区广顺北大街33号望京凯德MALL B1', 116.4712, 39.9965, 95, 0000002160, 0000000002, 50, '11:30-22:30', '2021-12-22 19:00:13', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (3, '京韵家常菜（西单大悦城店）', 1, 'https://p0.meituan.net/biztone/694233_1619500156517.jpeg,https://img.meituan.net/msmerchant/876ca8983f7395556eda9ceb064e6bc51840883.png,https://img.meituan.net/msmerchant/86a76ed53c28eff709a36099aefe28b51554088.png', '西单', '西城区西单北大街131号西单大悦城7F', 116.3745, 39.9088, 78, 0000012035, 0000000001, 47, '10:30-21:00', '2021-12-22 19:10:05', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (4, '左岸西餐厅（国贸店）', 1, 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '国贸', '朝阳区建国门外大街1号国贸商城3期B1', 116.4599, 39.9088, 290, 0000013519, 0000000001, 49, '11:00-22:00', '2021-12-22 19:17:15', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (5, '蜀香源老火锅（五道口店）', 1, 'https://p0.meituan.net/biztone/163160492_1624251899456.jpeg,https://img.meituan.net/msmerchant/59b7eff9b60908d52bd4aea9ff356e6d145920.jpg,https://qcloud.dpfile.com/pc/Qe2PTEuvtJ5skpUXKKoW9OQ20qc7nIpHYEqJGBStJx0mpoyeBPQOJE4vOdYZwm9AuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '五道口', '海淀区成府路28号优盛大厦F4', 116.3381, 39.9926, 104, 0000004125, 0000000002, 40, '10:00-24:00', '2021-12-22 19:20:58', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (6, '老北京铜锅涮肉（后海店）', 1, 'https://img.meituan.net/msmerchant/e71a2d0d693b3033c15522c43e03f09198239.jpg,https://img.meituan.net/msmerchant/9f8a966d60ffba00daf35458522273ca658239.jpg,https://img.meituan.net/msmerchant/ef9ca5ef6c05d381946fe4a9aa7d9808554502.jpg', '后海', '西城区烟袋斜街甲8号', 116.3860, 39.9400, 130, 0000009531, 0000000000, 46, '11:00-13:50,17:00-20:50', '2021-12-22 19:24:53', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (7, '渔家傲烤鱼（朝阳大悦城店）', 1, 'https://img.meituan.net/msmerchant/909434939a49b36f340523232924402166854.jpg,https://img.meituan.net/msmerchant/32fd2425f12e27db0160e837461c10303700032.jpg,https://img.meituan.net/msmerchant/f7022258ccb8dabef62a0514d3129562871160.jpg', '朝阳大悦城', '朝阳区朝阳北路101号朝阳大悦城6F', 116.5170, 39.9230, 88, 0000002631, 0000000001, 47, '11:00-22:00', '2021-12-22 19:40:52', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (8, '江户前寿司（亮马桥店）', 1, 'https://img.meituan.net/msmerchant/cf3dff697bf7f6e11f4b79c4e7d989e4591290.jpg,https://img.meituan.net/msmerchant/0b463f545355c8d8f021eb2987dcd0c8567811.jpg,https://img.meituan.net/msmerchant/c3c2516939efaf36c4ccc64b0e629fad587907.jpg', '亮马桥', '朝阳区亮马桥路48号院燕莎友谊商城B1', 116.4640, 39.9500, 92, 0000002406, 0000000001, 46, '11:00-21:30', '2021-12-22 19:51:06', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (9, '塞北羊蝎子炭火锅（双井店）', 1, 'https://p0.meituan.net/biztone/163160492_1624251899456.jpeg,https://img.meituan.net/msmerchant/e478eb16f7e31a7f8b29b5e3bab6de205500837.jpg,https://img.meituan.net/msmerchant/6173eb1d18b9d70ace7fdb3f2dd939662884857.jpg', '双井', '朝阳区东三环南路富力广场F5', 116.4645, 39.8925, 101, 0000002763, 0000000001, 50, '11:00-21:30', '2021-12-22 19:53:59', '2026-07-21 12:58:46');
INSERT INTO `tb_shop` VALUES (10, '星聚会量贩KTV（三里屯店）', 2, 'https://p0.meituan.net/joymerchant/a575fd4adb0b9099c5c410058148b307-674435191.jpg,https://p0.meituan.net/merchantpic/68f11bf850e25e437c5f67decfd694ab2541634.jpg,https://p0.meituan.net/dpdeal/cb3a12225860ba2875e4ea26c6d14fcc197016.jpg', '三里屯', '朝阳区工体北路8号院世茂工三F4', 116.4460, 39.9350, 67, 0000026891, 0000000000, 37, '00:00-24:00', '2021-12-22 20:25:16', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (11, '欢唱一百KTV（国贸店）', 2, 'https://p0.meituan.net/dpmerchantpic/53e74b200211d68988a4f02ae9912c6c1076826.jpg,https://qcloud.dpfile.com/pc/4iWtIvzLzwM2MGgyPu1PCDb4SWEaKqUeHm--YAt1EwR5tn8kypBcqNwHnjg96EvT_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/WZsJWRI447x1VG2x48Ujgu7vwqksi_9WitdKI4j3jvIgX4MZOpGNaFtM93oSSizbGybIjx5eX6WNgCPvcASYAw.jpg', '国贸', '朝阳区光华路9号世贸天阶F6', 116.4640, 39.9130, 75, 0000035977, 0000000000, 47, '11:30-06:00', '2021-12-22 20:29:02', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (12, '夜之魅CLUB（工体店）', 2, 'https://p0.meituan.net/dpmerchantpic/63833f6ba0393e2e8722420ef33f3d40466664.jpg,https://p0.meituan.net/dpmerchantpic/ae3c94cc92c529c4b1d7f68cebed33fa105810.png,', '工体', '朝阳区工体西路6号', 116.4480, 39.9330, 88, 0000006444, 0000000000, 46, '10:00-02:00', '2021-12-22 20:34:34', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (13, '音浪量贩KTV（中关村店）', 2, 'https://p1.meituan.net/merchantpic/598c83a8c0d06fe79ca01056e214d345875600.jpg,https://qcloud.dpfile.com/pc/HhvI0YyocYHRfGwJWqPQr34hRGRl4cWdvlNwn3dqghvi4WXlM2FY1te0-7pE3Wb9_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/F5ZVzZaXFE27kvQzPnaL4V8O9QCpVw2nkzGrxZE8BqXgkfyTpNExfNG5CEPQX4pjGybIjx5eX6WNgCPvcASYAw.jpg', '中关村', '海淀区中关村大街15号中关村广场F5', 116.3160, 39.9830, 58, 0000018997, 0000000000, 41, '12:00-02:00', '2021-12-22 20:38:54', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (14, '声动量贩KTV（亚运村店）', 2, 'https://p0.meituan.net/dpmerchantpic/f4cd6d8d4eb1959c3ea826aa05a552c01840451.jpg,https://p0.meituan.net/dpmerchantpic/2efc07aed856a8ab0fc75c86f4b9b0061655777.jpg,https://qcloud.dpfile.com/pc/zWfzzIorCohKT0bFwsfAlHuayWjI6DBEMPHHncmz36EEMU9f48PuD9VxLLDAjdoU_Gd2X_f-v9T8Yj4uLt25Gg.jpg', '亚运村', '朝阳区安慧里四区15号', 116.4000, 39.9900, 60, 0000017771, 0000000000, 47, '10:00-22:00', '2021-12-22 20:48:54', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (20, '拾光咖啡（三里屯店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/coffee.jpg', '三里屯', '朝阳区三里屯路19号太古里北区N4', 116.4551, 39.9355, 45, 0000008920, 0000000000, 45, '07:00-22:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (21, '京汤拉面（西单大悦城店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/lamian.jpg', '西单', '西城区西单北大街131号西单大悦城B1', 116.3740, 39.9090, 68, 0000005600, 0000000000, 46, '11:00-21:30', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (22, '胡同小馆（南锣鼓巷店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/xinbailu.jpg', '南锣鼓巷', '东城区南锣鼓巷87号', 116.4030, 39.9370, 72, 0000015200, 0000000000, 48, '10:30-14:00,16:30-21:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (23, '鮨町日本料理（三里屯店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/riben.jpg', '三里屯', '朝阳区三里屯路11号院', 116.4560, 39.9370, 380, 0000002100, 0000000000, 49, '11:30-14:00,17:00-22:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (24, '巴黎甜点工坊（国贸店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/fashi.jpg', '国贸', '朝阳区建国门外大街1号国贸商城1层', 116.4600, 39.9090, 120, 0000003200, 0000000000, 47, '10:00-22:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (25, '渝州老灶火锅（望京店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/dayu.jpg', '望京', '朝阳区望京广顺南大街16号', 116.4680, 39.9930, 130, 0000007800, 0000000000, 46, '11:00-23:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (26, '桂香居（前门大街店）', 1, 'https://my-project3.oss-cn-beijing.aliyuncs.com/guimanlong.jpg', '前门', '东城区前门大街68号', 116.3980, 39.8990, 88, 0000009800, 0000000000, 48, '10:00-14:00,16:30-21:30', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (30, '纯K歌会所（金融街店）', 2, 'https://my-project3.oss-cn-beijing.aliyuncs.com/cunKTV.jpg', '金融街', '西城区金融大街7号', 116.3620, 39.9150, 98, 0000004500, 0000000000, 44, '12:00-02:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (31, '麦浪KTV（五棵松店）', 2, 'https://my-project3.oss-cn-beijing.aliyuncs.com/KTV22.jpg', '五棵松', '海淀区复兴路69号五棵松华熙LIVE B1', 116.2750, 39.9100, 55, 0000007200, 0000000000, 46, '11:00-06:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (35, '尚品美发（西单店）', 3, 'https://my-project3.oss-cn-beijing.aliyuncs.com/meifa111.jpg', '西单', '西城区西单北大街110号', 116.3730, 39.9080, 280, 0000001800, 0000000000, 46, '10:00-21:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (36, '格调造型（三里屯旗舰店）', 3, 'https://my-project3.oss-cn-beijing.aliyuncs.com/meifa222.jpg', '三里屯', '朝阳区三里屯路19号太古里南区S5', 116.4540, 39.9340, 160, 0000002500, 0000000000, 44, '09:30-20:30', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (40, '力健健身（国贸店）', 4, 'https://my-project3.oss-cn-beijing.aliyuncs.com/jianshen111.jpg', '国贸', '朝阳区光华路9号世贸天阶B1', 116.4630, 39.9140, 99, 0000012000, 0000000000, 47, '06:00-23:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (41, '燃动健身（中关村店）', 4, 'https://my-project3.oss-cn-beijing.aliyuncs.com/jianshen222.jpg', '中关村', '海淀区中关村大街19号新中关B1', 116.3150, 39.9820, 120, 0000008600, 0000000000, 48, '07:00-22:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (45, '悦享足道（亚运村店）', 5, 'https://my-project3.oss-cn-beijing.aliyuncs.com/huaxinliangzi.jpg', '亚运村', '朝阳区安慧里三区10号', 116.4030, 39.9880, 168, 0000003800, 0000000000, 45, '10:00-02:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (50, '微醺酒吧（后海店）', 8, 'https://my-project3.oss-cn-beijing.aliyuncs.com/jiubar111.jpg', '后海', '西城区后海北沿8号', 116.3880, 39.9420, 200, 0000006500, 0000000000, 44, '20:00-04:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');
INSERT INTO `tb_shop` VALUES (51, '蓝调爵士吧（三里屯店）', 8, 'https://my-project3.oss-cn-beijing.aliyuncs.com/jiubar2222.jpg', '三里屯', '朝阳区三里屯路4号院', 116.4520, 39.9360, 150, 0000004200, 0000000000, 47, '18:00-02:00', '2026-07-06 09:59:51', '2026-07-21 12:54:15');

-- ----------------------------
-- Table structure for tb_shop_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_shop_type
-- ----------------------------
INSERT INTO `tb_shop_type` VALUES (1, '美食', 'https://my-project3.oss-cn-beijing.aliyuncs.com/ms.png', 1, '2021-12-22 20:17:47', '2026-07-05 20:34:26');
INSERT INTO `tb_shop_type` VALUES (2, 'KTV', 'https://my-project3.oss-cn-beijing.aliyuncs.com/KTV.png', 2, '2021-12-22 20:18:27', '2026-07-05 20:34:26');
INSERT INTO `tb_shop_type` VALUES (3, '丽人·美发', 'https://my-project3.oss-cn-beijing.aliyuncs.com/meifa.png', 3, '2021-12-22 20:18:48', '2026-07-05 20:15:53');
INSERT INTO `tb_shop_type` VALUES (4, '健身运动', 'https://my-project3.oss-cn-beijing.aliyuncs.com/js.png', 10, '2021-12-22 20:19:04', '2026-07-05 20:16:55');
INSERT INTO `tb_shop_type` VALUES (5, '按摩·足疗', 'https://my-project3.oss-cn-beijing.aliyuncs.com/am.png', 5, '2021-12-22 20:19:27', '2026-07-05 20:17:55');
INSERT INTO `tb_shop_type` VALUES (6, '美容SPA', 'https://my-project3.oss-cn-beijing.aliyuncs.com/SPA.png', 6, '2021-12-22 20:19:35', '2026-07-05 20:18:56');
INSERT INTO `tb_shop_type` VALUES (7, '亲子游乐', 'https://my-project3.oss-cn-beijing.aliyuncs.com/youle.png', 7, '2021-12-22 20:19:53', '2026-07-05 20:20:08');
INSERT INTO `tb_shop_type` VALUES (8, '酒吧', 'https://my-project3.oss-cn-beijing.aliyuncs.com/jiuba.png', 8, '2021-12-22 20:20:02', '2026-07-05 20:21:09');
INSERT INTO `tb_shop_type` VALUES (9, '轰趴馆', 'https://my-project3.oss-cn-beijing.aliyuncs.com/hongba.png', 9, '2021-12-22 20:20:08', '2026-07-05 20:22:26');
INSERT INTO `tb_shop_type` VALUES (10, '美睫·美甲', 'https://my-project3.oss-cn-beijing.aliyuncs.com/meija.png', 4, '2021-12-22 20:21:46', '2026-07-05 20:23:28');

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1023 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, '13686869696', 'salt01userfish12345@9a53a8cca9ce47ec5f875f2783e229be', '小鱼同学', 'https://my-project3.oss-cn-beijing.aliyuncs.com/20241125135432.jpg', '2021-12-24 10:27:19', '2026-07-06 00:10:24');
INSERT INTO `tb_user` VALUES (2, '13838411438', 'salt02userkeai12345@444f8121bf43adbd32a2c38adc3ed9f0', '可可今天不吃肉', 'https://my-project3.oss-cn-beijing.aliyuncs.com/20241125135422.jpg', '2021-12-24 15:14:39', '2026-07-05 21:58:26');
INSERT INTO `tb_user` VALUES (4, '13456789011', 'salt04userlx1234567@3800d5272b2cb3f2b21806aee6687228', 'user_slxaxy2au9f3tanffaxr', 'https://my-project3.oss-cn-beijing.aliyuncs.com/20241125135432.jpg', '2022-01-07 12:07:53', '2026-07-05 21:59:04');
INSERT INTO `tb_user` VALUES (5, '13456789001', 'salt05userkeaido12@7b560cc63906d472b1cc4047fd2c0dae', '可爱多', 'https://my-project3.oss-cn-beijing.aliyuncs.com/20241125135427.jpg', '2022-01-07 16:11:33', '2026-07-10 22:37:02');
INSERT INTO `tb_user` VALUES (1012, '15555555555', '6mh31cggktxqk2x2am5v@3de2f346df8774de479dc00e57a412b6', '张梓涵', 'https://my-project3.oss-cn-beijing.aliyuncs.com/20241125135432.jpg', '2026-07-05 21:42:11', '2026-07-15 14:44:43');
INSERT INTO `tb_user` VALUES (1017, '15555532680', 'dov54h76fmhcblmhkrnp@dea7f1e80c5c4c7c300b19a1ca2af960', '美女', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/5/1/c8ff676b-51ee-498b-877a-ba0cb2c6bb0d.jpg', '2026-07-05 22:23:57', '2026-07-05 22:23:57');
INSERT INTO `tb_user` VALUES (1018, '15689512456', 'b7kuoy65pw78jh157lro@ae1282a7dc4e4eda90b01fd0ce1bb2d6', '法师', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/2/8/8ae628ae-724c-42a6-a54b-6b46c535a600.jpg', '2026-07-05 22:26:46', '2026-07-05 22:26:46');
INSERT INTO `tb_user` VALUES (1019, '15236985412', 't2jzrowvwh49ckekjl71@038c3b58db3ec01209f0c7ae2de5558a', '野猪骑士', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/2/1/6f7690e3-24ed-4498-adfe-1fc67f4a4af7.jpg', '2026-07-05 22:27:27', '2026-07-05 22:27:27');
INSERT INTO `tb_user` VALUES (1020, '15698542565', '42kep675k0p3mgsj91ui@7974a6c591783159414353173b1df21c', '加工费', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/15/1/7785a165-1c70-469a-bc26-2729ba4f7de4.jpg', '2026-07-05 22:43:34', '2026-07-05 22:43:34');
INSERT INTO `tb_user` VALUES (1021, '15689521458', 'xi9kdyjw106zxd0mdvn3@15f90a60a3da010ea992bb01cebe6057', '安哥拉', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/10/3/e0e6e51f-8da2-4d12-bf24-142a81406626.jpg', '2026-07-05 22:44:07', '2026-07-05 22:44:07');
INSERT INTO `tb_user` VALUES (1022, '15689521036', 'b6qwuf4t0xyvy28o2nlv@2d1e7fc9007f355d430378a4a44e8bf7', '距离', 'https://my-project3.oss-cn-beijing.aliyuncs.com/blogs/5/15/0ebeba69-ca46-4aab-b34e-97b03a9f946c.jpg', '2026-07-06 00:02:06', '2026-07-06 00:02:06');

-- ----------------------------
-- Table structure for tb_user_badge
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_badge`;
CREATE TABLE `tb_user_badge`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `badge_id` bigint NOT NULL COMMENT '勋章ID',
  `earned_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_badge`(`user_id` ASC, `badge_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_badge
-- ----------------------------
INSERT INTO `tb_user_badge` VALUES (1, 5, 1, '2026-07-20 23:02:12');
INSERT INTO `tb_user_badge` VALUES (2, 2, 10, '2026-07-21 09:12:27');
INSERT INTO `tb_user_badge` VALUES (3, 1, 10, '2026-07-21 12:39:10');
INSERT INTO `tb_user_badge` VALUES (4, 1, 1, '2026-07-21 12:41:07');

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
  `fans` int UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
  `gender` tinyint UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `total_sign_days` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计签到天数',
  `last_sign_date` date NULL DEFAULT NULL COMMENT '最近签到日期',
  `total_blogs` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计发布笔记数',
  `total_likes` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计获赞数',
  `total_badges` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '已获得勋章数',
  `level` tinyint UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------
INSERT INTO `tb_user_info` VALUES (1, '北京', '热爱美食的程序员', 4, 3, 1, '1995-06-15', 926, 2, '2026-07-22', 1, 2, 2, 1, '2026-07-05 20:34:26', '2026-07-22 12:46:29');
INSERT INTO `tb_user_info` VALUES (2, '上海', '探店达人，吃遍全国', 4, 3, 0, '1998-03-22', 2507, 1, '2026-07-21', 0, 2, 1, 1, '2026-07-05 20:34:26', '2026-07-21 17:16:26');
INSERT INTO `tb_user_info` VALUES (4, '深圳', '周末去哪吃', 3, 3, 1, '1994-01-30', 501, 0, NULL, 0, 1, 0, 0, '2026-07-05 20:34:26', '2026-07-21 12:39:27');
INSERT INTO `tb_user_info` VALUES (5, '北京', '咖啡爱好者', 4, 3, 0, '1997-11-08', 421, 3, '2026-07-22', 1, 2, 1, 1, '2026-07-05 20:34:26', '2026-07-22 12:14:14');
INSERT INTO `tb_user_info` VALUES (1012, '北京', '爱吃爱玩的大学生', 0, 3, 0, '2002-08-20', 355, 0, NULL, 0, 0, 0, 1, '2026-07-06 00:07:58', '2026-07-21 10:15:29');
INSERT INTO `tb_user_info` VALUES (1017, '北京', '美食探店达人', 0, 0, 0, '1996-05-12', 1200, 0, NULL, 0, 0, 0, 3, '2026-07-06 00:07:58', '2026-07-21 00:24:39');
INSERT INTO `tb_user_info` VALUES (1018, '上海', '游戏美食两不误', 0, 0, 1, '1999-11-01', 180, 0, NULL, 0, 0, 0, 0, '2026-07-06 00:07:58', '2026-07-21 00:24:39');
INSERT INTO `tb_user_info` VALUES (1019, '深圳', '周末就是用来吃的', 0, 0, 1, '1995-03-28', 520, 0, NULL, 0, 0, 0, 2, '2026-07-06 00:07:58', '2026-07-21 00:24:39');

-- ----------------------------
-- Table structure for tb_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '商铺id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代金券标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint UNSIGNED NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
  `actual_value` bigint NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
  `type` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通券；1,秒杀券',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_voucher
-- ----------------------------
INSERT INTO `tb_voucher` VALUES (1, 1, '50元代金券', '周一至周日均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 4750, 5000, 0, 1, '2022-01-04 09:42:39', '2022-01-04 09:43:31');
INSERT INTO `tb_voucher` VALUES (10, 2, '30元代金券', '周一至周五可用', '全场通用\n无需预约\n仅限堂食', 2500, 3000, 0, 1, '2026-07-05 20:35:03', '2026-07-05 20:35:03');
INSERT INTO `tb_voucher` VALUES (11, 3, '100元代金券', '周末可用', '全场通用\n可叠加使用\n不兑现', 8000, 10000, 0, 1, '2026-07-05 20:35:03', '2026-07-05 20:35:03');
INSERT INTO `tb_voucher` VALUES (12, 5, '50元火锅券', '周一至周日可用', '仅限火锅类\n可叠加\n不找零', 4000, 5000, 0, 1, '2026-07-05 20:35:03', '2026-07-05 20:35:03');
INSERT INTO `tb_voucher` VALUES (13, 1, '80元代金券', '工作日可用', '全场通用\n需预约', 6000, 8000, 0, 1, '2026-07-05 20:35:03', '2026-07-05 20:35:03');
INSERT INTO `tb_voucher` VALUES (14, 4, '150元代金券', '全年可用', '全场通用\n不限时段', 12000, 15000, 0, 1, '2026-07-05 20:35:03', '2026-07-05 20:35:03');
INSERT INTO `tb_voucher` VALUES (15, 1, '1元秒杀50元券', '限时秒杀', '每人限购1张\n不可退款', 100, 5000, 1, 1, '2026-07-05 20:35:03', '2026-07-11 15:05:05');
INSERT INTO `tb_voucher` VALUES (16, 5, '1元秒杀80元券', '疯狂抢购', '每人限购1张\n售完即止', 100, 8000, 1, 1, '2026-07-05 20:35:03', '2026-07-11 15:05:07');
INSERT INTO `tb_voucher` VALUES (17, 2, '1元秒杀30元券', '限量抢购', '每人限购1张\n先到先得', 100, 3000, 1, 1, '2026-07-05 20:35:03', '2026-07-11 15:05:09');
INSERT INTO `tb_voucher` VALUES (29, 6, '老北京铜锅涮肉（后海店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (30, 7, '渔家傲烤鱼（朝阳大悦城店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (31, 8, '江户前寿司（亮马桥店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (32, 9, '塞北羊蝎子炭火锅（双井店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (33, 10, '星聚会量贩KTV（三里屯店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (34, 11, '欢唱一百KTV（国贸店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (35, 12, '夜之魅CLUB（工体店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (36, 13, '音浪量贩KTV（中关村店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (37, 14, '声动量贩KTV（亚运村店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (38, 20, '拾光咖啡（三里屯店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (39, 21, '京汤拉面（西单大悦城店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (40, 22, '胡同小馆（南锣鼓巷店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (41, 23, '鮨町日本料理（三里屯店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (42, 24, '巴黎甜点工坊（国贸店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (43, 25, '渝州老灶火锅（望京店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (44, 26, '桂香居（前门大街店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (45, 30, '纯K歌会所（金融街店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (46, 31, '麦浪KTV（五棵松店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (47, 35, '尚品美发（西单店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (48, 36, '格调造型（三里屯旗舰店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (49, 40, '力健健身（国贸店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (50, 41, '燃动健身（中关村店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (51, 45, '悦享足道（亚运村店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (52, 50, '微醺酒吧（后海店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (53, 51, '蓝调爵士吧（三里屯店） 专享满减券', '店内消费满100元可用', '仅限本店使用\n满100元可用\n不可与其他优惠同享\n不兑现、不找零', 800, 1500, 0, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (60, 1, '川渝老灶火锅（三里屯店） 1元秒杀50元券', '限时秒杀，库存50张', '每人限购1张\n仅限本店使用\n售完即止\n不可退款', 100, 5000, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (61, 5, '蜀香源老火锅（五道口店） 1元秒杀80元券', '限时秒杀，库存40张', '每人限购1张\n仅限本店使用\n售完即止\n不可退款', 100, 8000, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (62, 10, '星聚会量贩KTV（三里屯店） 1元秒杀2小时欢唱券', '限时秒杀，库存30张', '每人限购1张\n仅限本店使用\n需提前预约\n不可退款', 100, 6000, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (63, 20, '拾光咖啡（三里屯店） 1元秒杀30元代金券', '限时秒杀，库存60张', '每人限购1张\n仅限本店使用\n售完即止\n不可退款', 100, 3000, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (64, 35, '尚品美发（西单店） 1元秒杀洗剪吹券', '限时秒杀，库存25张', '每人限购1张\n仅限本店使用\n售完即止\n不可退款', 100, 8800, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (65, 40, '力健健身（国贸店） 1元秒杀单次体验券', '限时秒杀，库存80张', '每人限购1张\n仅限本店使用\n首次到店可用\n不可退款', 100, 9900, 1, 1, '2026-07-21 09:06:30', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (66, NULL, '平台通用50元代金券', '积分兑换，全场商家通用', '全部商家可用\n不兑现、不找零\n每人限兑1张', 3000, 5000, 0, 1, '2026-07-21 09:18:03', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (67, NULL, '平台通用80元代金券', '积分兑换，全场商家通用', '全部商家可用\n不兑现、不找零\n每人限兑1张', 5000, 8000, 0, 1, '2026-07-21 09:18:03', '2026-07-21 09:25:46');
INSERT INTO `tb_voucher` VALUES (68, NULL, '平台通用100元代金券', '积分兑换，全场商家通用', '全部商家可用\n不兑现、不找零\n每人限兑1张', 8000, 10000, 0, 1, '2026-07-21 09:18:03', '2026-07-21 09:25:46');

-- ----------------------------
-- Table structure for tb_voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '下单的用户id',
  `voucher_id` bigint UNSIGNED NOT NULL COMMENT '购买的代金券id',
  `pay_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `verify_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核销码',
  `shop_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT 'Applicable shop ID; NULL means universal voucher',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;

-- ----------------------------
-- Records of tb_voucher_order
-- ----------------------------
INSERT INTO `tb_voucher_order` VALUES (616731868909333090, 5, 1, 2, 2, '2026-07-20 23:14:38', '2026-07-20 23:14:40', NULL, NULL, '2026-07-20 23:58:54', '687321', 1);
INSERT INTO `tb_voucher_order` VALUES (616734892566309475, 5, 12, 2, 2, '2026-07-20 23:26:22', '2026-07-20 23:26:25', NULL, NULL, '2026-07-20 23:58:54', '203767', 5);
INSERT INTO `tb_voucher_order` VALUES (616739148878899812, 5, 13, 2, 2, '2026-07-20 23:42:53', '2026-07-20 23:42:55', NULL, NULL, '2026-07-20 23:58:54', '627427', 1);
INSERT INTO `tb_voucher_order` VALUES (616743933472407553, 5, 10, 4, 2, '2026-07-21 00:01:27', '2026-07-21 00:01:27', NULL, NULL, '2026-07-21 00:01:27', '369976', NULL);
INSERT INTO `tb_voucher_order` VALUES (617278141504684033, 1, 60, 2, 2, '2026-07-22 10:34:28', '2026-07-22 10:34:29', NULL, NULL, '2026-07-22 10:34:29', '821027', 1);
INSERT INTO `tb_voucher_order` VALUES (617278193044291586, 1, 48, 2, 2, '2026-07-22 10:34:39', '2026-07-22 10:34:41', NULL, NULL, '2026-07-22 10:34:41', '768223', 36);
INSERT INTO `tb_voucher_order` VALUES (617286838813458435, 1, 51, 2, 2, '2026-07-22 11:08:12', '2026-07-22 11:08:14', NULL, NULL, '2026-07-22 11:08:14', '258226', 45);
INSERT INTO `tb_voucher_order` VALUES (617312166235602948, 1, 66, 4, 2, '2026-07-22 12:46:29', '2026-07-22 12:46:29', NULL, NULL, '2026-07-22 12:46:29', '167574', NULL);

SET FOREIGN_KEY_CHECKS = 1;
