DROP TABLE IF EXISTS `fpcy_role_log`;
CREATE TABLE `fpcy_role_log` (
  `logId` varchar(32) NOT NULL,
  `poolId` int(10) DEFAULT NULL COMMENT '确认唯一标识',
  `newRole` varchar(100) DEFAULT NULL COMMENT '规则',
  `newRoleJsName` varchar(100) DEFAULT NULL COMMENT '规则Js名称',
  `oldRole` varchar(100) DEFAULT NULL COMMENT '规则',
  `oldRoleJsName` varchar(100) DEFAULT NULL COMMENT '规则Js名称',
  `inputTime` datetime DEFAULT NULL COMMENT '录入时间',
  PRIMARY KEY (`logId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发票规则流水';

-- ----------------------------
-- Records of fpcy_role_log
-- ----------------------------

-- ----------------------------
-- Table structure for fpcy_taxoffice_role
-- ----------------------------
DROP TABLE IF EXISTS `fpcy_taxoffice_role`;
CREATE TABLE `fpcy_taxoffice_role` (
  `poolId` int(11) NOT NULL AUTO_INCREMENT,
  `swjg_mc` varchar(255) DEFAULT NULL COMMENT 'swjg_mc 税务机构名称',
  `role` varchar(100) DEFAULT NULL COMMENT '规则',
  `roleJsName` varchar(100) DEFAULT NULL COMMENT '规则Js名称',
  `isUse` char(1) DEFAULT NULL COMMENT '是否停用  0:是,1否 (人工设置)',
  `stopTime` datetime DEFAULT NULL COMMENT '停用时间',
  `inputTime` datetime DEFAULT NULL COMMENT '录入时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`poolId`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8 COMMENT='发票规则';

-- ----------------------------
-- Records of fpcy_taxoffice_role
-- ----------------------------
INSERT INTO `fpcy_taxoffice_role` VALUES ('1', '北京增值税（货运专票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('2', '北京增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('3', '北京增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('4', '北京增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('5', '天津增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('6', '天津增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('7', '天津增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('8', '河北增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('9', '河北增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('10', '河北增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('11', '山西增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('12', '山西增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('13', '山西增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('14', '内蒙古增值税(流向)', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('15', '内蒙古增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('16', '内蒙古增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('17', '内蒙古增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('18', '辽宁增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('19', '辽宁增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('20', '辽宁增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('21', '大连增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('22', '大连增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('23', '大连增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('24', '吉林增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('25', '吉林增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('26', '吉林增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('27', '黑龙江增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('28', '黑龙江增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('29', '黑龙江增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('30', '上海增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('31', '上海增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('32', '上海增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('33', '江苏增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('34', '江苏增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('35', '江苏增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('36', '浙江增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('37', '浙江增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('38', '浙江增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('39', '宁波增值税（普通发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('40', '宁波增值税（专用发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('41', '宁波增值税（电子发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('42', '安徽增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('43', '安徽增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('44', '安徽增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('45', '福建增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('46', '福建增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('47', '福建增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('48', '厦门增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('49', '厦门增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('50', '厦门增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('51', '江西增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('52', '江西增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('53', '江西增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('54', '山东增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('55', '山东增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('56', '山东增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('57', '青岛增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('58', '青岛增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('59', '青岛增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('60', '河南增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('61', '河南增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('62', '河南增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('63', '湖北增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('64', '湖北增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('65', '湖北增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('66', '湖南增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('67', '湖南增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('68', '湖南增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('69', '广东增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('70', '广东增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('71', '广东增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('72', '深圳增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('73', '深圳增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('74', '深圳增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('75', '广西增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('76', '广西增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('77', '广西增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('78', '海南增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('79', '海南增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('80', '海南增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('81', '重庆增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('82', '重庆增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('83', '重庆增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('84', '四川增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('85', '四川增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('86', '四川增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('87', '贵州增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('88', '贵州增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('89', '贵州增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('90', '云南增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('91', '云南增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('92', '云南增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('93', '西藏增值税（普通发票）', '□☺8>4☺386☺-18☺◇', '32e01', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('94', '西藏增值税（专用发票）', '□☺8>4☺386☺-18☺◇', '32e01', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('95', '西藏增值税（电子发票）', '□☺8>4☺386☺-18☺◇', '32e01', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('96', '陕西增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('97', '陕西增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('98', '陕西增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('99', '甘肃增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('100', '甘肃增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('101', '甘肃增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('102', '青海增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('103', '青海增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('104', '青海增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('105', '宁夏增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('106', '宁夏增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('107', '宁夏增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('108', '新疆增值税（普通发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('109', '新疆增值税（专用发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('110', '新疆增值税（电子发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('111', '北京增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('112', '天津增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('113', '河北增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('114', '山西增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('115', '内蒙古增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('116', '辽宁增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('117', '大连增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('118', '吉林增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('119', '黑龙江增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('120', '上海增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('121', '江苏增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('122', '浙江增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('123', '宁波增值税（机动车销售统一发票）', '□☺2>4_7>1☺59☺-23☺▽', '582ae', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('124', '安徽增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('125', '福建增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('126', '厦门增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('127', '江西增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('128', '山东增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('129', '青岛增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('130', '河南增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('131', '湖北增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('132', '湖南增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('133', '广东增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('134', '深圳增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('135', '广西增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('136', '海南增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('137', '重庆增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('138', '四川增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('139', '贵州增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('140', '云南增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('141', '西藏增值税（机动车销售统一发票）', '□☺8>4☺386☺-18☺◇', '32e01', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('142', '陕西增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('143', '甘肃增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('144', '青海增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('145', '宁夏增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
INSERT INTO `fpcy_taxoffice_role` VALUES ('146', '新疆增值税（机动车销售统一发票）', '▽☺4>2_8>6☺467☺-4☺□', 'dc1de', '0', null, null, null);
