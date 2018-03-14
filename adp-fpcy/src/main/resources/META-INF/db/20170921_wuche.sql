DROP TABLE IF EXISTS `fpcy_request_log`;
CREATE TABLE `fpcy_request_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `requestId`varchar(64) DEFAULT NULL COMMENT '请求id(框架生成id)',
  `invoiceNum` varchar(10) DEFAULT NULL COMMENT '发票号码',
  `invoiceCode` varchar(20) DEFAULT NULL COMMENT '发票代码',
  `invoiceName` varchar(100) DEFAULT NULL COMMENT '发票类型名称',
  `requestType` char(10) DEFAULT NULL COMMENT '请求类型  yzm：验证码请求  cx:查询请求',
  `requestConent` text COMMENT '查验参数',
  `errorMsg` longtext COMMENT '错误结果',
  `errorCode` varchar(100) DEFAULT NULL COMMENT '错误码',
  `requestTime` varchar(10) DEFAULT NULL COMMENT '请求时长',
  `isSuccess` char(1) DEFAULT NULL COMMENT '是否发送成功 Y：是 N：否',
  `comeFromCode` char(1) DEFAULT NULL COMMENT '来源',
  `inputTime` datetime DEFAULT NULL COMMENT '录入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发票_请求_流水';


DROP TABLE IF EXISTS `ocr_request_log`;
CREATE TABLE `ocr_request_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `requestId`varchar(64) DEFAULT NULL COMMENT '请求id(框架生成id)',
  `codeType` char(6) DEFAULT NULL COMMENT '验证码类型',
  `picId` varchar(64) DEFAULT NULL COMMENT '识别业务单号',
  `yzm` varchar(64) DEFAULT NULL COMMENT '识别内容',
  `reviceTime` datetime DEFAULT NULL COMMENT '获取时间',
  `recogTime` datetime DEFAULT NULL COMMENT '识别时间',
  `dateLength` char(10) DEFAULT NULL COMMENT '识别时长',
  `company` varchar(50) DEFAULT NULL COMMENT '识别厂家',
  `isRight` char(1) DEFAULT NULL,
  `inputTime` datetime DEFAULT NULL COMMENT '录入时间',
  `updateTime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='验证码打码流水';

DROP TABLE IF EXISTS `ocr_request_setting`;
CREATE TABLE `ocr_request_setting` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `codeType` varchar(10) DEFAULT NULL COMMENT '验证码类型',
  `thirdCodeType` varchar(25) DEFAULT NULL COMMENT '第三方验证码类型',
  `thirdCom` varchar(10) DEFAULT NULL COMMENT '使用第三方服务，1超级鹰，2联众',
  `remark` varchar(200) DEFAULT NULL COMMENT '验证码说明',
  `inputTime` datetime DEFAULT NULL COMMENT '添加日期',
  `inputUserCode` varchar(50) DEFAULT NULL COMMENT '添加人',
  `updateTime` datetime DEFAULT NULL COMMENT '修改日期',
  `updateUserCode` varchar(50) DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6038 DEFAULT CHARSET=utf8;

INSERT INTO `ocr_request_setting` VALUES (1001, '1001', '1901', '1', '四个字母,数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1002, '1002', '1902', '1', '常见4位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1003, '1003', '1101', '1', '1位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1004, '1004', '1004', '1', '1~4位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1005, '1005', '1005', '1', '1~5位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1006, '1006', '1006', '1', '1~6位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1007, '1007', '1007', '1', '1~7位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1008, '1008', '1008', '1', '1~8位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1009, '1009', '1009', '1', '1~9位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1010, '1010', '1010', '1', '1~10位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1011, '1011', '1012', '1', '1~12位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1012, '1012', '1020', '1', '1~20位英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1013, '1013', '1023', '2', '带文字描述的字母数字题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1014, '1014', '1025', '2', '仅输入数字部分', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1015, '1015', '1009', '2', '纯数字验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1016, '1016', '1013', '2', '5位字母加数字(5位纯字母）', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1017, '1017', '1014', '2', '6位字母加数字（6位纯字母）', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1018, '1018', '1015', '2', '7位字母加数字（7位纯字母）', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1019, '1019', '1016', '2', '两位数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1020, '1020', '1017', '2', '8位或8位以上字母', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (1021, '1021', '1026', '2', '固定长度4字母数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2001, '2001', '2002', '1', '1~2位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2002, '2002', '2003', '1', '1~3位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2003, '2003', '2004', '1', '1~4位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2004, '2004', '2005', '1', '1~5位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2005, '2005', '2006', '1', '1~6位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2006, '2006', '2007', '1', '1~7位纯汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2007, '2007', '1012', '2', '1位中文汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2008, '2008', '1101', '2', '2位中文汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2009, '2009', '1104', '2', '3位中文汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2010, '2010', '1105', '2', '4位中文汉字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (2011, '2011', '1108', '2', '中文验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3001, '3001', '3004', '1', '1~4位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3002, '3002', '3005', '1', '1~5位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3003, '3003', '3006', '1', '1~6位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3004, '3004', '3007', '1', '1~7位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3005, '3005', '3008', '1', '1~8位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3006, '3006', '3012', '1', '1~12位纯英文', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (3007, '3007', '1008', '2', '纯字母验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4001, '4001', '4004', '1', '1~4位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4002, '4002', '4005', '1', '1~5位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4003, '4003', '4006', '1', '1~6位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4004, '4004', '4007', '1', '1~7位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4005, '4005', '4008', '1', '1~8位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4006, '4006', '4111', '1', '11位纯数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4007, '4007', '1004', '2', '数字验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4008, '4008', '1031', '2', '点阵数字题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (4009, '4009', '1027', '2', '带箭头方向顺序的数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5000, '5000', '5000', '1', '不定长汉字英文数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5001, '5001', '1020', '2', '汉字转阿拉伯数字题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5002, '5002', '1010', '2', '看图题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5003, '5003', '1011', '2', '要求题目', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5004, '5004', '1018', '2', '动态验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5005, '5005', '5108', '1', '8位英文数字(包含字符)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5006, '5006', '5201', '1', '拼音首字母，计算题，成语混合', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (5007, '5007', '5211', '1', '集装箱号 4位字母7位数字', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6001, '6001', '6001', '1', '计算题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6002, '6002', '6003', '1', '复杂计算题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6003, '6003', '6002', '1', '选择题四选一(ABCD或1234)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6004, '6004', '6004', '1', '问答题，智能回答题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6005, '6005', '9101', '1', '坐标选一,返回格式:x,y', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6006, '6006', '9102', '1', '点击两个相同的字,返回:x1,y1|x2,y2', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6007, '6007', '9202', '1', '点击两个相同的动物或物品,返回:x1,y1|x2,y2', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6008, '6008', '9103', '1', '坐标多选,返回3个坐标,如:x1,y1|x2,y2|x3,y3', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6009, '6009', '9004', '1', '坐标多选,返回1~4个坐标,如:x1,y1|x2,y2|x3,y3', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6010, '6010', '9104', '1', '坐标选四,返回格式:x1,y1|x2,y2|x3,y3|x4,y4', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6011, '6011', '9201', '1', '坐标多选,返回1~5个坐标值', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6012, '6012', '1005', '2', '安卓右旋验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6013, '6013', '1006', '2', '安卓左旋转验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6014, '6014', '1019', '2', '图片旋转180度', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6015, '6015', '1021', '2', '选择图片序号', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6016, '6016', '1022', '2', '两段英文二选一', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6017, '6017', '1024', '2', '看图选序号(左旋90度)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6018, '6018', '1030', '2', '位置选择题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6019, '6019', '1032', '2', '选择出现个数题', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6020, '6020', '1102', '2', '九宫格验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6021, '6021', '1106', '2', '阳光验证码', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6022, '6022', '1107', '2', '带上特殊符号', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6023, '6023', '1109', '2', '识别汉字首字母', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6024, '6024', '1111', '2', '识别成语', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6025, '6025', '1112', '2', '车牌号识别', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6026, '6026', '1301', '2', '原型验证码(坐标)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6027, '6027', '1302', '2', '谷歌验证码(坐标)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6028, '6028', '1303', '2', '坐标题点击若干次', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6029, '6029', '1306', '2', '安卓坐标题(左旋)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6030, '6030', '1307', '2', '安卓坐标题(右旋)', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6031, '6031', '1309', '2', '坐标题，点击所有光点', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6032, '6032', '1310', '2', '拼图的验证码。', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6033, '6033', '1311', '2', '坐标题点击1次', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6034, '6034', '1312', '2', '坐标题点击2次', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6035, '6035', '1313', '2', '坐标题点击3次', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6036, '6036', '1314', '2', '坐标题点击4次', NULL, NULL, NULL, NULL);
INSERT INTO `ocr_request_setting` VALUES (6037, '6037', '1321', '2', '坐标题点击若干次(&分隔)', NULL, NULL, NULL, NULL);

update dm_fpcycs set yzm_dz='https://fpcy.qh-n-tax.gov.cn/WebQuery/yzmQuery'
, cy_dz='https://fpcy.qh-n-tax.gov.cn/WebQuery/query'
, cyym='https://fpcy.qh-n-tax.gov.cn/WebQuery/yzmQuery'
 where swjg_dm='6300';

update dm_fpcycs set yzm_dz='https://fpcy.qh-n-tax.gov.cn/WebQuery/yzmQuery'
, cy_dz='https://fpcy.qh-n-tax.gov.cn/WebQuery/query'
, cyym='https://fpcy.qh-n-tax.gov.cn/WebQuery/yzmQuery'
where swjg_dm='16300' and fp_zl='111';