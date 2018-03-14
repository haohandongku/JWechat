-- 修改服务可用性
-- 浙江
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ('13300', '111', 'gb', '313');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ('3300', '111', 'gb', '138');
INSERT INTO `fpcy_enablequery` (`swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ('3300', '222', 'gb', '177');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ('3300', '333', 'gb', '213');
-- 江苏
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '13200', '111', 'gb', '312');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '3200', '111', 'gb', '169');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '3200', '222', 'gb', '208');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '3200', '333', 'gb', '244');

-- 河北
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '14200', '111', 'gb', '323');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '4200', '111', 'gb', '153');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '4200', '222', 'gb', '192');
INSERT INTO `fpcy_enablequery` ( `swjg_dm`, `fp_zl`, `query`, `poolid`) VALUES ( '4200', '333', 'gb', '228');


update `fpcy_enablequery` set `query`='kq' where swjg_dm='4200';
update `fpcy_enablequery` set `query`='kq' where swjg_dm='14200'and `fp_zl`='111';

update `fpcy_enablequery` set `query`='kq' where swjg_dm='3200';
update `fpcy_enablequery` set `query`='kq' where swjg_dm='13200' and `fp_zl`='111';

update `fpcy_enablequery` set `query`='kq' where swjg_dm='3300';
update `fpcy_enablequery` set `query`='kq' where swjg_dm='13300' and `fp_zl`='111';