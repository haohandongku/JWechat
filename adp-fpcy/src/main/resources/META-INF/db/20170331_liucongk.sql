ALTER TABLE `fpcy_requeststatistics_log`
MODIFY COLUMN `requestStatus`  char(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求结果状态（0：成功  1：系统请求失败 2:税局查询失败）' AFTER `expectTime`;

INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1100', '', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1400', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1400', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1500', '000', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1500', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('1500', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2100', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2100', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2102', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2102', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2102', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('2300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3302', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3302', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3302', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3400', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3400', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3500', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3500', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3502', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3502', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3502', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3600', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3600', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3600', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3700', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3700', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3700', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3702', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3702', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('3702', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4100', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4100', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4200', '999', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4400', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4400', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4500', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4500', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4600', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4600', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4600', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('4600', '888', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5000', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5000', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5000', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5100', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5100', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5400', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('5400', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6100', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6100', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6200', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6200', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6300', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6300', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6400', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6400', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6500', '222', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('6500', '333', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('11100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('11200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('11300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('11400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('11500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('12100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('12102', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('12200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('12300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13302', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13302', 'null', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13502', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13600', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13700', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('13702', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14403', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14600', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('14600', 'null', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('15000', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('15100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('15200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('15300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('15400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16100', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16100', 'null', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16200', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16300', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16400', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('16500', '111', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('23300', 'null', '1');
INSERT INTO fpcy_servicestate(`swjg_dm`, `fp_zl`, `state`) VALUES ('23502', 'null', '1');


-- 天津(0)
update fpcy_servicestate set state ='0' where swjg_dm='1200';
-- 河北(0)
update fpcy_servicestate set state ='0' where swjg_dm='1300';
-- 山西(0)
update fpcy_servicestate set state ='0' where swjg_dm='1400';
-- 内蒙古(0)
update fpcy_servicestate set state ='0' where swjg_dm='1500';
-- 辽宁(0)
update fpcy_servicestate set state ='0' where swjg_dm='2100';
-- 大连(特殊五个省市)(0)
update fpcy_servicestate set state ='0' where swjg_dm='2102';
-- 吉林(0)
update fpcy_servicestate set state ='0' where swjg_dm='2200';
-- 黑龙江(0)
update fpcy_servicestate set state ='0' where swjg_dm='2300';
-- 上海(0)
update fpcy_servicestate set state ='0' where swjg_dm='3100';
-- 江苏(0)
update fpcy_servicestate set state ='0' where swjg_dm='3200';
-- 浙江(0)
update fpcy_servicestate set state ='0' where swjg_dm='3300';
-- 宁波(特殊五个城市)(0)(没有数据)
update fpcy_servicestate set state ='0' where swjg_dm='3302';
-- 安徽(0)
update fpcy_servicestate set state ='0' where swjg_dm='3400';
-- 福建(0)
update fpcy_servicestate set state ='0' where swjg_dm='3500';
-- 厦门(特殊五个城市)(0)
update fpcy_servicestate set state ='0' where swjg_dm='3502';
-- 江西(0)
update fpcy_servicestate set state ='0' where swjg_dm='3600';
-- 山东(0)
update fpcy_servicestate set state ='0' where swjg_dm='3700';
-- 青岛(特殊五个城市)(0)
update fpcy_servicestate set state ='0' where swjg_dm='3702';
--  ===先开通==  -- 
-- 河南
update fpcy_servicestate set state ='0' where swjg_dm='4100';
-- 湖北
update fpcy_servicestate set state ='0' where swjg_dm='4200';
-- 湖南
update fpcy_servicestate set state ='0' where swjg_dm='4300';
-- 广东
update fpcy_servicestate set state ='0' where swjg_dm='4400';
-- 深圳(特殊五个城市)
update fpcy_servicestate set state ='0' where swjg_dm='4403';
-- 广西
update fpcy_servicestate set state ='0' where swjg_dm='4500';
-- 海南
update fpcy_servicestate set state ='0' where swjg_dm='4600';
-- 重庆
update fpcy_servicestate set state ='0' where swjg_dm='5000';
-- 四川
update fpcy_servicestate set state ='0' where swjg_dm='5100';
-- 贵州
update fpcy_servicestate set state ='0' where swjg_dm='5200';
-- 云南
update fpcy_servicestate set state ='0' where swjg_dm='5300';
-- 西藏
update fpcy_servicestate set state ='0' where swjg_dm='5400';
-- 陕西
update fpcy_servicestate set state ='0' where swjg_dm='6100';
-- 甘肃
update fpcy_servicestate set state ='0' where swjg_dm='6200';
-- 青海
update fpcy_servicestate set state ='0' where swjg_dm='6300';
-- 宁夏
update fpcy_servicestate set state ='0' where swjg_dm='6400';
-- 新疆
update fpcy_servicestate set state ='0' where swjg_dm='6500';




