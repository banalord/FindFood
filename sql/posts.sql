
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` bigint(20) NOT NULL COMMENT '帖子ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of posts
-- ----------------------------
INSERT INTO `posts` VALUES (1, 'Spring Boot 入门', '本篇介绍 Spring Boot 的基本用法', 52);
INSERT INTO `posts` VALUES (2, 'MyBatis-Plus 实战', '学习如何使用 MP 快速开发增删改查接口', 4);
INSERT INTO `posts` VALUES (3, 'Redis 缓存设计', '设计点赞数缓存方案，提高系统吞吐量', 56);
INSERT INTO `posts` VALUES (4, 'MySQL 调优指南', '通过慢查询分析和索引优化提升查询效率', 104);
INSERT INTO `posts` VALUES (5, '分布式系统入门', '探讨常见分布式架构方案及 CAP 理论', 20);
INSERT INTO `posts` VALUES (6, 'Nginx 反向代理实战', '配置负载均衡和动静分离', 8);
INSERT INTO `posts` VALUES (7, 'Java 多线程详解', '深入理解线程池、并发工具类等', 66);
INSERT INTO `posts` VALUES (8, '消息队列对比', '比较 Kafka, RabbitMQ, RocketMQ 等方案', 8);
INSERT INTO `posts` VALUES (9, 'ElasticSearch 搜索引擎', '实现全文检索的最佳实践', 49);
INSERT INTO `posts` VALUES (10, 'Linux 运维基础', '熟练使用常见命令与服务配置', 100);

SET FOREIGN_KEY_CHECKS = 1;
