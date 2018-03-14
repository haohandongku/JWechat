DROP TABLE IF EXISTS `fpcy_requeststatistics_log`;
CREATE TABLE `fpcy_requeststatistics_log` (
  `requestId` varchar(20) NOT NULL COMMENT '请求日志ID',
  `parameter` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '请求参数',
  `result` text COMMENT '返回结果',
  `requestTime` varchar(10) DEFAULT NULL COMMENT '请求时长',
  `expectTime` varchar(10) DEFAULT NULL COMMENT '期望请求时长',
  `requestStatus` char(1) DEFAULT NULL COMMENT '请求结果状态（0：成功  1：系统请求失败 2:税局查询失败）',
  `inputTime` datetime DEFAULT NULL COMMENT '录入时间',
  `invoiceType` varchar(50) DEFAULT NULL COMMENT '票种',
  `fphm` varchar(20) DEFAULT NULL COMMENT '发票号码',
  `fpdm` varchar(20) DEFAULT NULL COMMENT '发票代码',
  `comeFromCode` char(1) DEFAULT NULL COMMENT '数据来源',
  PRIMARY KEY (`requestId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='请求成功失败统计';

DROP TABLE IF EXISTS `SEQUENCE_FPCY_REQUESTID`;
 CREATE TABLE `SEQUENCE_FPCY_REQUESTID` (
`ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计表ID',
PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统计表ID';

DROP PROCEDURE IF EXISTS `P_SEQUENCE_FPCY_REQUESTID`;
CREATE PROCEDURE `P_SEQUENCE_FPCY_REQUESTID`(out sequenceNo  VARCHAR(20),in sequenceName VARCHAR(20))
COMMENT '功能描述：请求ID
           输入参数：请求ID
           输出参数：序列值'
begin
DECLARE ac_jdh VARCHAR(10);
DECLARE ac_no int;
set ac_jdh = 'req';
INSERT INTO SEQUENCE_FPCY_REQUESTID VALUES(NULL);
SELECT LAST_INSERT_ID() into ac_no;
set sequenceNo = CONCAT(ac_jdh, DATE_FORMAT(CURDATE(), '%y'), '9', lpad(CONCAT(ac_no), 8, '0'), '000');
end;

DROP TABLE IF EXISTS `fpcy_servicestate`;
CREATE TABLE `fpcy_servicestate` (
  `swjg_dm` varchar(50) NOT NULL DEFAULT '' COMMENT '税务机构代码',
  `fp_zl` varchar(50) NOT NULL DEFAULT '' COMMENT '发票种类',
  `state` char(1) DEFAULT NULL COMMENT '启用状态',
  PRIMARY KEY (`swjg_dm`,`fp_zl`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `fpcy_servicestate` VALUES ('1100', '111', '0');
INSERT INTO `fpcy_servicestate` VALUES ('1100', '222', '0');
INSERT INTO `fpcy_servicestate` VALUES ('1100', '333', '0');
INSERT INTO `fpcy_servicestate` VALUES ('11100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('11200', '000', '1');
INSERT INTO `fpcy_servicestate` VALUES ('11300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('11400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('12100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('12102', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('12200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('12300', '2', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13100', '111', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13201', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13502', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13600', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13700', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('13702', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14201', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14403', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('14500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('15000', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('15100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('15201', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('15300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('15400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('16200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('16400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('16500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('2100', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('21100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('21500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('2200', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('2200', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22102', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22200', '1', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22200', '2', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22200', '3', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22200', '4', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('22300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('2300', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23302', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23600', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23700', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('23702', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('24600', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('25000', '1', '1');
INSERT INTO `fpcy_servicestate` VALUES ('25000', '2', '1');
INSERT INTO `fpcy_servicestate` VALUES ('25101', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('25200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('25300', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('26100', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('26101', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('26200', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('26400', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('26500', 'null', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3100', '111', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3100', '222', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3100', '333', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3200', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3200', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3400', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3502', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3502', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3600', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('3702', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4300', '888', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4300', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4403', '111', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4403', '222', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4403', '333', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4500', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4500', '888', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4500', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('4600', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('5000', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('5100', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('5100', '888', '1');
INSERT INTO `fpcy_servicestate` VALUES ('5100', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('5200', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6300', '777', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6300', '888', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6300', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6400', '999', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6500', '888', '1');
INSERT INTO `fpcy_servicestate` VALUES ('6500', '999', '1');

DROP TABLE IF EXISTS `sequence_fpcy_cyno`;
CREATE TABLE `sequence_fpcy_cyno` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '核心表ID',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8 COMMENT='核心表ID';


CREATE PROCEDURE `P_SEQUENCE_FPCY_NO`(out sequenceNo  VARCHAR(20),in sequenceName VARCHAR(20))
begin
DECLARE ac_jdh VARCHAR(10);
DECLARE ac_no int;
set ac_jdh = 'red';
INSERT INTO sequence_fpcy_cyno VALUES(NULL);
SELECT LAST_INSERT_ID() into ac_no;
set sequenceNo = CONCAT(ac_jdh, DATE_FORMAT(CURDATE(), '%y'), '9', lpad(CONCAT(ac_no), 8, '0'), '000');
end;
