-- 清理历史数据
DELETE from sequence_xh;
DELETE from xt_xtcs;
commit;

-- 初始化系统参数
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('10000', '系统名称', '企业服务平台', '设置系统名称', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('10001', '通用序号前缀', 'XH', '通用序号前缀', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('40001', '中文字体库存放根地址', '/usr/local/font/', 'PDF导出时需要使用的中文字体库存放根路径', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('30001', '文件存放根地址', '/u01/upload', '设置文件保存根路径', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);

commit;