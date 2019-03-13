/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50724
 Source Host           : localhost:3306
 Source Schema         : deploy

 Target Server Type    : MySQL
 Target Server Version : 50724
 File Encoding         : 65001

 Date: 13/03/2019 17:00:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_upd_pkg
-- ----------------------------
DROP TABLE IF EXISTS `app_upd_pkg`;
CREATE TABLE `app_upd_pkg` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `appId` int(8) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `updateDesc` varchar(255) DEFAULT NULL,
  `optime` datetime DEFAULT NULL,
  `type` int(1) DEFAULT NULL,
  `locked` int(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of app_upd_pkg
-- ----------------------------
BEGIN;
INSERT INTO `app_upd_pkg` VALUES (1, 1, '2019031201', '初始版本', '2019-03-12 10:56:24', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for base_app
-- ----------------------------
DROP TABLE IF EXISTS `base_app`;
CREATE TABLE `base_app` (
  `appId` int(8) NOT NULL AUTO_INCREMENT,
  `appName` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `describes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`appId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of base_app
-- ----------------------------
BEGIN;
INSERT INTO `base_app` VALUES (1, 'platform', 'D://workspace/platform', '公共平台');
INSERT INTO `base_app` VALUES (2, 'phis', 'D://workspace/phis', '基层医疗');
INSERT INTO `base_app` VALUES (3, 'chis', 'D://workspace/chis', '公共卫生');
COMMIT;

-- ----------------------------
-- Table structure for base_slave
-- ----------------------------
DROP TABLE IF EXISTS `base_slave`;
CREATE TABLE `base_slave` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `describes` varchar(255) DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of base_slave
-- ----------------------------
BEGIN;
INSERT INTO `base_slave` VALUES (1, '本地节点1', '127.0.0.1', '本地节点1', NULL);
COMMIT;

-- ----------------------------
-- Table structure for base_user
-- ----------------------------
DROP TABLE IF EXISTS `base_user`;
CREATE TABLE `base_user` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  `createDt` date DEFAULT NULL,
  `loginName` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of base_user
-- ----------------------------
BEGIN;
INSERT INTO `base_user` VALUES (1, '管理员', '202cb962ac59075b964b07152d234b70', 1, '2019-03-12', 'admin');
INSERT INTO `base_user` VALUES (2, NULL, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for slave_app
-- ----------------------------
DROP TABLE IF EXISTS `slave_app`;
CREATE TABLE `slave_app` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `appId` int(8) DEFAULT NULL,
  `slaveId` int(8) DEFAULT NULL,
  `pkgId` int(8) DEFAULT NULL,
  `app_target_path` varchar(255) DEFAULT NULL,
  `app_backup_path` varchar(255) DEFAULT NULL,
  `app_tomcat_home` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of slave_app
-- ----------------------------
BEGIN;
INSERT INTO `slave_app` VALUES (1, 2, 1, NULL, '/Users/yangl/Develop/tomcat7/webapps/phis/', '', '/Users/yangl/Develop/tomcat7/');
INSERT INTO `slave_app` VALUES (3, 1, 1, NULL, '', '', '');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
