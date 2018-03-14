-- 修改之前一张role表
drop table if exists fpcy_role_log;
create table fpcy_role_log
(
   id                   int(11) not null auto_increment comment '唯一标识  主键',
   swjg_mc              varchar(50),
   newRole              varchar(100) comment '规则',
   newRoleJsName        varchar(100) comment '规则Js名称',
   oldRole              varchar(100),
   oldRoleJsName        varchar(100),
   inputTime            datetime,
   primary key (id)
);
alter table fpcy_role_log comment '发票规则流水';

