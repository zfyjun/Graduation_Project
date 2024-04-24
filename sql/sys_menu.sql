/*
Navicat MySQL Data Transfer

Source Server         : cc
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : exam

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2024-04-23 18:44:09
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `page_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COMMENT='112';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('1', '主页', '/home', 'el-icon-house', '主页', null, 'Home');
INSERT INTO `sys_menu` VALUES ('2', '用户管理', '/user', 'el-icon-document', '用户管理', '5', 'User');
INSERT INTO `sys_menu` VALUES ('3', '角色管理', '/role', 'el-icon-s-custom', '角色管理', '5', 'Role');
INSERT INTO `sys_menu` VALUES ('4', '文件管理', '/file', 'el-icon-document', '文件管理', '5', 'File');
INSERT INTO `sys_menu` VALUES ('5', '系统管理', '', 'el-icon-menu', '系统管理', null, null);
INSERT INTO `sys_menu` VALUES ('9', '菜单管理', '/menu', 'el-icon-s-grid', '菜单管理', '5', 'Menu');
