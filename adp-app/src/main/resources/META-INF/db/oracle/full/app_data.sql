
-- 初始化系统参数
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('10000', '系统名称', '企业服务平台', '设置系统名称', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('10001', '通用序号前缀', 'XH', '通用序号前缀', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
INSERT INTO xt_xtcs (CSXH,CSMC,CSNR,SYSM,XYBZ,JZSZBZ,JGBM,LRR,LRRQ,XGR,XGRQ) VALUES ('40001', '中文字体库存放根地址', '/usr/local/font/', 'PDF导出时需要使用的中文字体库存放根路径', 'Y', 'Y', 'PUBLIC', '000000000000', SYSDATE(), NULL, NULL);
commit;
-- Create sequence 
create sequence SEQUENCE_XH
minvalue 1
maxvalue 999999999
start with 1
increment by 1
cache 20
cycle;

create or replace procedure P_SEQUENCE_XH(sequenceNo out VARCHAR2,sequenceName in  VARCHAR2)
--2002.9.11 zoujd add
 is
    lvc_jdh    varchar2(4);
    avc_xh  number(10);
begin

    select CSNR into lvc_jdh from XT_XTCS where CSXH = '10001';
    if lvc_jdh is null then
         lvc_jdh := '-1';
    end if;
    select to_char(SEQUENCE_XH.nextval) into avc_xh from dual;
    avc_xh := lpad(avc_xh,9,'0');
    sequenceNo := lvc_jdh||to_char(sysdate,'yy')||avc_xh||'000';   
end;
