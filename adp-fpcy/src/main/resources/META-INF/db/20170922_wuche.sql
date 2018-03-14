ALTER TABLE `fpcy_request_log`
MODIFY COLUMN `errorCode`  varchar(1000) CHARACTER SET
 utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '错误信息代码' AFTER `errorMsg`;