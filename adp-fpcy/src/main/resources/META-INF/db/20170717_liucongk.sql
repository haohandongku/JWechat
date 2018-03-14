ALTER TABLE `vat_invoicequerysuccessrecord`
MODIFY COLUMN `invoiceQueryResult`  longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `uid`;

CREATE TABLE `vat_requeststatistics_log` (
  `requestId` varchar(35) NOT NULL COMMENT '请求日志ID',
  `customerId` varchar(35) DEFAULT NULL COMMENT '用户编码',
  `fphm` varchar(25) DEFAULT NULL COMMENT '发票号码',
  `fpdm` varchar(25) DEFAULT NULL COMMENT '发票代码',
  `parameter` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '查验参数',
  `result` longtext COMMENT '返回结果',
  `requestStatus` char(5) DEFAULT NULL COMMENT '请求结果状态',
  `invoiceType` varchar(50) DEFAULT NULL COMMENT '发票种类',
  `comeFromCode` char(1) DEFAULT NULL COMMENT '数据来源',
  `inputTime` datetime DEFAULT NULL COMMENT '查验日期',
  PRIMARY KEY (`requestId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `fpcy_requeststatistics_log`
MODIFY COLUMN `result`  longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '返回结果' AFTER `parameter`;

ALTER TABLE `cy_cyrz`
MODIFY COLUMN `cyjg`  longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `uid`;

ALTER TABLE `cy_cyrz`
MODIFY COLUMN `invoiceResult`  longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL AFTER `useTime`;

