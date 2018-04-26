#0921�޸�
ALTER TABLE `base_station_management`.`base_station_info` 
CHANGE COLUMN `duration` `duration` DECIMAL(6,2) NULL DEFAULT NULL COMMENT 'Ԥ��ʱ������λСʱ' ;

ALTER TABLE `base_station_management`.`base_station_info` 
CHANGE COLUMN `duration_status` `duration_status` INT(11) NULL DEFAULT '1' COMMENT '1��2��3��4��' ;

ALTER TABLE `gprs_config_info`
ADD COLUMN `max_discharge_cur`  decimal(6,3) NULL DEFAULT 100.000 COMMENT '��Ч�ŵ�������ֵ' AFTER `margin_time`;

#109�޸�
ALTER TABLE `gprs_config_info`
ADD COLUMN `gprs_flag`  int(11) NULL DEFAULT 0 COMMENT '����������ʶ 1 ���� 0 ������';

ALTER TABLE `sub_devices` 
add COLUMN  `sub_flag` int(11) NULL DEFAULT 0 COMMENT '���ôӻ���ʶ 1 ���� 0 ������';

#10/10
ALTER TABLE `gprs_config_info`
ADD COLUMN `gprs_port` varchar(16) NULL DEFAULT null COMMENT '�����˿�';

ALTER TABLE `gprs_config_info`
ADD COLUMN `gprs_spec` varchar(16) NULL DEFAULT null COMMENT '�������';

ALTER TABLE `sub_devices`
ADD COLUMN `sub_spec` varchar(16) NULL DEFAULT null COMMENT '�ӻ��˿�';

ALTER TABLE `sub_devices`
ADD COLUMN `sub_type` int(11) NULL DEFAULT null COMMENT '�豸���ͣ�1���ش��������豸,2���ش�������������';

#10/16
ALTER TABLE `base_station_info`
ADD COLUMN `inspect_status`  int(11) NULL DEFAULT 0 COMMENT '��װ��ά�����̣������״̬�� 0:δ��װ��1:�Ѱ�װ��2:��װ�У�21:��װ�еȴ�ȷ��״̬��22:��װ�к�̨ȷ��δ���״̬��3:ά���У�31:ά���еȴ�ȷ��״̬��32:ά���к�̨ȷ��δ���״̬' AFTER `load_power`;

ALTER TABLE `routing_inspection_detail`
DROP COLUMN `detail_operator_type`;

ALTER TABLE `routing_inspections`
ADD COLUMN `confirm_operate_id`  int(11) NULL DEFAULT NULL COMMENT '��̨ȷ����ԱID' AFTER `gprs_id_device`,
ADD COLUMN `confirm_operate_name`  varchar(32) NULL DEFAULT NULL COMMENT '��̨ȷ����Աname' AFTER `confirm_operate_id`;
#10/20
ALTER TABLE `routing_inspection_detail`
ADD COLUMN `request_seq`  int(11) NULL DEFAULT null COMMENT 'app�˵�һ��������1���ڶ���������2,�Դ�����';

ALTER TABLE `routing_inspection_detail`
ADD COLUMN `request_type`  int(11) NULL DEFAULT 0 COMMENT 'web�˻�Ӧ״̬0��û�л�Ӧ��1 �ǻ�Ӧ��Ĭ����0';
#10/26
ALTER TABLE `pulse_discharge_info`
ADD COLUMN `filter_voltage`  varchar(10000) NULL DEFAULT null COMMENT '���˺�ĵ�ѹ';

ALTER TABLE `pulse_discharge_info`
ADD COLUMN `filter_current`  varchar(10000) NULL DEFAULT null COMMENT '���˺�ĵ���';

#10/31
ALTER TABLE `roles`
MODIFY COLUMN `role_id`  int(11) NOT NULL AUTO_INCREMENT FIRST ;

#11/1
CREATE TABLE `role_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#11/6
ALTER TABLE `base_station_info`
ADD COLUMN `address_coding`  varchar(32) NULL DEFAULT null COMMENT 'վַ���';
#11/7
ALTER TABLE `routing_inspections`
ADD COLUMN `operate_phone`  varchar(32) NULL DEFAULT null COMMENT '������Ա�绰'

#11/7
CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `amplifi`@`%` 
    SQL SECURITY DEFINER
VIEW `full_station_info` AS
    SELECT 
        `st`.`id` AS `id`,
        `st`.`gprs_id` AS `gprs_id`,
        `st`.`gprs_id_out` AS `gprs_id_out`,
        `st`.`name` AS `name`,
        `st`.`address` AS `address`,
        `st`.`province` AS `province`,
        `st`.`city` AS `city`,
        `st`.`district` AS `district`,
        `st`.`lat` AS `lat`,
        `st`.`lng` AS `lng`,
        `st`.`maintainance_id` AS `maintainance_id`,
        `st`.`pack_type` AS `pack_type`,
        `st`.`room_type` AS `room_type`,
        `st`.`duration` AS `duration`,
        `st`.`real_duration` AS `real_duration`,
        `st`.`ok_num` AS `ok_num`,
        `st`.`poor_num` AS `poor_num`,
        `st`.`error_num` AS `error_num`,
        `st`.`status` AS `status`,
        `st`.`company_id1` AS `company_id1`,
        `st`.`company_id2` AS `company_id2`,
        `st`.`company_id3` AS `company_id3`,
        `st`.`del_flag` AS `del_flag`,
        `st`.`company_name3` AS `company_name3`,
        `st`.`vol_level` AS `vol_level`,
        `st`.`operator_type` AS `operator_type`,
        `st`.`duration_status` AS `duration_status`,
        `st`.`update_time` AS `update_time`,
        `st`.`load_power` AS `load_power`,
        `st`.`inspect_status` AS `inspect_status`,
        `st`.`address_coding` AS `address_coding`,
        `config`.`link_status` AS `link_status`,
        `config`.`device_type` AS `device_type`,
        `pack`.`state` AS `state`,
        `pack`.`gen_vol` AS `gen_vol`,
        `pack`.`gen_cur` AS `gen_cur`
    FROM
        ((`base_station_info` `st`
        LEFT JOIN `gprs_config_info` `config` ON ((`st`.`gprs_id` = `config`.`gprs_id`)))
        LEFT JOIN `pack_data_info_latest` `pack` ON ((`config`.`gprs_id` = `pack`.`gprs_id`)))

#//21
insert into city_code (city_code,city_name,parent_code,TYPE) 
values ('510125','ֱϽ��','442000',4),('510118','����','442000',4),('510119','����','442000',4),('510120','����','442000',4),('510123','����','442000',4)

insert into city_code (city_code,city_name,parent_code,TYPE) 
values ('510126','ֱϽ��','441900',4)

#11/22
ALTER TABLE `routing_inspections`
ADD COLUMN `remark`  varchar(255) NULL COMMENT 'Ѳ���¼��ע' AFTER `operate_phone`;

#11/23
CREATE TABLE `base_station_management`.`device_discharge_autocheck` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `gprs_id` VARCHAR(12) NULL,
  `first_discharge_time` DATETIME NULL COMMENT '��һ�ηŵ�ʱ��',
  `start_vol` DECIMAL(6,2) NULL COMMENT '��ʼ��ѹ',
  `end_vol` DECIMAL(6,2) NULL COMMENT '������ѹ',
  `is_correct` INT NULL COMMENT '����״̬�Ƿ���ȷ���ŵ��ѹ���ͼ�Ϊ��ȷ��',
  `check_date` DATETIME NULL COMMENT '���ʱ�䣬д��ü�¼��ʱ��',
  `correct_date` DATETIME NULL COMMENT '�豸״̬�޸���ʱ��',
  PRIMARY KEY (`id`))
COMMENT = '�豸�ŵ�����״̬�жϱ�';

#11/23
ALTER TABLE `device_discharge_autocheck`
ADD COLUMN `data_updated`  int(11) NULL DEFAULT 0 COMMENT '�����Ƿ����޸� 0 δ�޸���1 �޸�';

#11/28
ALTER TABLE `routing_inspections`
ADD COLUMN `gprs_device_type`  varchar(32) NULL DEFAULT null COMMENT '�豸����';

#11/29
ALTER TABLE `users`
DROP INDEX `user_phone_UNIQUE`;

#11/29
alter table routing_inspections change gprs_device_type device_type varchar(32) DEFAULT NULL
alter table routing_inspections change gprs_id_device gprs_id varchar(12) DEFAULT NULL

#12/5
ALTER TABLE `parameters`
MODIFY COLUMN `parameter_value`  varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL AFTER `parameter_code`;

#12/8
delete from city_code where city_name  in ('中沙群岛的岛礁及其海域' , '西沙群岛', '南沙群岛')
update city_code set city_name ='海南省直辖行政单位' where city_code = '469000'
update city_code set city_name ='湖北省直辖行政单位' where city_code = '429000'

 #12/14
 ALTER TABLE `gprs_config_info`
ADD COLUMN `charge_interval`  int(11) NULL DEFAULT '60' COMMENT '充电状态下状态帧传输间隔，单位秒';

ALTER TABLE `gprs_config_send`
ADD COLUMN `charge_interval`  int(11) NULL DEFAULT '60' COMMENT '充电状态下状态帧传输间隔，单位秒';
#12/17
ALTER TABLE `users`
ADD COLUMN `user_code`  int(11) NULL DEFAULT null COMMENT '用户验证码';
#12/20
alter table gprs_balance_send add column mode int(11) null default null comment '1：强制执行，从机均衡状态由后台控制； 0：非强制，从机可以启用自动均衡'

#12/25
alter table base_station_info 
add column cell_count int(11) null default null comment '电池总数';

alter table gprs_config_info 
add column sub_device_count int(11) null default null comment '从机总数';

 CREATE TABLE `cell_vol_level` (                                                                         
                  `id` int(11) NOT NULL AUTO_INCREMENT,                                                                 
                  `vol_level_name` varchar(32) DEFAULT NULL COMMENT '电压平台名称',                               
                  `vol_level_code` int(11) DEFAULT NULL COMMENT '电压平台编码',                                   
                  `create_id` varchar(32) DEFAULT NULL COMMENT '登录人员id',                                               
                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',  
                  PRIMARY KEY (`id`) USING BTREE                                                                        
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8  
  
 CREATE TABLE `gprs_device_type` (                                                                       
                    `id` int(11) NOT NULL AUTO_INCREMENT,                                                                 
                    `type_code` int(11) DEFAULT NULL COMMENT '设备类型编号',                                        
                    `type_name` varchar(32) DEFAULT NULL COMMENT '设备类型',                                          
                    `sub_vol` decimal(5,3) unsigned DEFAULT NULL COMMENT '从机电压',                                  
                    `vol_level` int(11) DEFAULT NULL COMMENT '电压平台编码 FK',                                     
                    `create_id` varchar(32) DEFAULT NULL COMMENT '登录人员id',                                         
                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',  
                    PRIMARY KEY (`id`)                                                                                    
                  ) ENGINE=InnoDB DEFAULT CHARSET=utf8   
                  

 #12/27                 
ALTER TABLE `base_station_management`.`parameters` 
CHANGE COLUMN `parameter_category` `parameter_category` VARCHAR(32) CHARACTER SET 'utf8' NOT NULL COMMENT '参数类别，用以区分同一个参数名，不同的设备或者分公司。' AFTER `parameter_code`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`parameter_code`, `parameter_category`);

ALTER TABLE `gprs_device_type`
ADD COLUMN sub_device_count  int(11) NULL DEFAULT null COMMENT '从机数量';  

#12/29
ALTER TABLE base_station_info alter column inspect_status drop default;
ALTER TABLE base_station_info alter column inspect_status set default 99; 
#2018/1/3

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for gprs_device_type
-- ----------------------------
DROP TABLE IF EXISTS `gprs_device_type`;
CREATE TABLE `gprs_device_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type_code` int(11) DEFAULT NULL COMMENT '设备类型编号',
  `type_name` varchar(32) DEFAULT NULL COMMENT '设备类型 ，1蓄电池串联复用设备,2蓄电池串联复用诊断组件，3，蓄电池4V监测设备，4，蓄电池12V监测设备',
  `sub_vol` decimal(5,3) unsigned DEFAULT NULL COMMENT '从机电压',
  `vol_level` int(11) DEFAULT NULL COMMENT '电压平台编码 FK',
  `create_id` varchar(32) DEFAULT NULL COMMENT '登录人员id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `sub_device_count` int(11) DEFAULT NULL COMMENT '从机数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of gprs_device_type
-- ----------------------------
INSERT INTO `gprs_device_type` VALUES ('1', '1', '蓄电池串联复用设备', '4.000', '2', 'admin', '2017-12-27 14:43:24', '24');
INSERT INTO `gprs_device_type` VALUES ('2', '2', '蓄电池串联复用诊断组件', '4.000', '2', 'admin', '2017-12-27 14:44:21', '24');
INSERT INTO `gprs_device_type` VALUES ('3', '3', '蓄电池2V监测设备', '4.000', '2', 'admin', '2017-12-27 14:45:31', '24');
INSERT INTO `gprs_device_type` VALUES ('4', '4', '蓄电池12V监测设备', '4.000', '12', 'admin', '2017-12-27 14:45:59', '4');  

#2018/1/3
INSERT INTO `permissions` (`permission_type`, `permission_name`, `permission_code`, `parent_id`, `permission_system`) VALUES ('1', '单体电压平台管理', '10003', '1', '1') ;
INSERT INTO `permissions` (`permission_type`, `permission_name`, `permission_code`, `parent_id`, `permission_system`) VALUES ('1', '设备类型管理', '10004', '1', '1') ;  

#2018/1/5
SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for cell_vol_level
-- ----------------------------
DROP TABLE IF EXISTS `cell_vol_level`;
CREATE TABLE `cell_vol_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `vol_level_name` varchar(32) DEFAULT NULL COMMENT '电压平台名称',
  `vol_level_code` int(11) DEFAULT NULL COMMENT '电压平台编码',
  `create_id` varchar(32) DEFAULT NULL COMMENT '登录人员id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of cell_vol_level
-- ----------------------------
INSERT INTO `cell_vol_level` VALUES ('1', '12', '12', 'admin', '2017-12-29 14:01:50');
INSERT INTO `cell_vol_level` VALUES ('2', '2', '2', 'admin', '2017-12-29 14:02:25');

#2018/18
CREATE INDEX rcv_time ON warning_info (gprs_id,rcv_time)


#2018/1/9  `st`.`load_current` AS `load_current`,
ALTER 
ALGORITHM=UNDEFINED 
DEFINER=`amplifi`@`%` 
SQL SECURITY DEFINER 
VIEW `full_station_info` AS 
SELECT
	`st`.`id` AS `id`,
	`st`.`gprs_id` AS `gprs_id`,
	`st`.`gprs_id_out` AS `gprs_id_out`,
	`st`.`name` AS `name`,
	`st`.`address` AS `address`,
	`st`.`province` AS `province`,
	`st`.`city` AS `city`,
	`st`.`district` AS `district`,
	`st`.`lat` AS `lat`,
	`st`.`lng` AS `lng`,
	`st`.`maintainance_id` AS `maintainance_id`,
	`st`.`pack_type` AS `pack_type`,
	`st`.`room_type` AS `room_type`,
	`st`.`duration` AS `duration`,
	`st`.`real_duration` AS `real_duration`,
	`st`.`ok_num` AS `ok_num`,
	`st`.`poor_num` AS `poor_num`,
	`st`.`error_num` AS `error_num`,
	`st`.`status` AS `status`,
	`st`.`company_id1` AS `company_id1`,
	`st`.`company_id2` AS `company_id2`,
	`st`.`company_id3` AS `company_id3`,
	`st`.`del_flag` AS `del_flag`,
	`st`.`company_name3` AS `company_name3`,
	`st`.`vol_level` AS `vol_level`,
	`st`.`operator_type` AS `operator_type`,
	`st`.`duration_status` AS `duration_status`,
	`st`.`update_time` AS `update_time`,
	`st`.`load_power` AS `load_power`,
	`st`.`inspect_status` AS `inspect_status`,
	`st`.`address_coding` AS `address_coding`,
	`st`.`cell_count` AS `cell_count`,
	`config`.`link_status` AS `link_status`,
	`config`.`device_type` AS `device_type`,
	`pack`.`state` AS `state`,
	`pack`.`gen_vol` AS `gen_vol`,
	`pack`.`gen_cur` AS `gen_cur`
FROM
	(
		(
			`base_station_info` `st`
			LEFT JOIN `gprs_config_info` `config` ON (
				(
					`st`.`gprs_id` = `config`.`gprs_id`
				)
			)
		)
		LEFT JOIN `pack_data_info_latest` `pack` ON (
			(
				`config`.`gprs_id` = `pack`.`gprs_id`
			)
		)
	) ;

#1/10
ALTER  TABLE  pack_data_info ALGORITHM=inplace, LOCK=NONE, DROP INDEX `rcv_index`,
ADD INDEX `rcv_index` (`rcv_time` ASC, `gprs_id` ASC, `state` ASC); 

#2018.1.22  分区的存储过程

DELIMITER $$

DROP PROCEDURE IF EXISTS `base_station_management`.`pack_date_info_partition`$$

CREATE DEFINER=`amplifi`@`%` PROCEDURE `pack_date_info_partition`()
BEGIN

		DECLARE pdate DATE;  		
		DECLARE num int DEFAULT 0; 
		DECLARE name_num int DEFAULT 1; 

			set pdate =DATE_FORMAT(now(),'%Y-%m-01');
			set @Max_date = (date_add(pdate, interval 2 MONTH));

			set @Max_name = (date_add(pdate, interval 1 MONTH)); 
			set @now_year = DATE_FORMAT(@Max_name,'%Y');
			set @now_month = DATE_FORMAT(@Max_name,'%m');
			SET @now_name= concat(@now_year,'',@now_month);
			set @query_max_name = concat('p',@now_name); -- 最大分区的名称

			set @del_date = (date_sub(pdate, interval 4 MONTH));
			set @del_year = DATE_FORMAT(@del_date,'%Y');
			set @del_month = DATE_FORMAT(@del_date,'%m');
			SET @del_name= concat(@del_year,'',@del_month);
			set @query_del_name = concat('p',@del_name); -- 删除，备份分区的名称

		 SELECT COUNT(partition_name) into num FROM INFORMATION_SCHEMA.partitions WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME='pack_data_info' and partition_name =@query_del_name;	
		
		IF num = 1 THEN 	
			-- 备份表
					set @back=concat('create table pack_data_info_',@query_del_name,' like pack_data_info');   
					 PREPARE stmt1 FROM @back;  
					 EXECUTE stmt1;  
					 DEALLOCATE PREPARE stmt1; 				
						
				-- 删除新表分区	
					set @deleNewPartition=concat('alter table pack_data_info_',@query_del_name,' REMOVE PARTITIONING');			
					 PREPARE stmt1 FROM @deleNewPartition;  
					 EXECUTE stmt1;  
					 DEALLOCATE PREPARE stmt1; 
						
		-- 交换分区
					set @exchangePartition=concat('alter table pack_data_info exchange partition ',@query_del_name,' with table pack_data_info_',@query_del_name);		
					 PREPARE stmt2 FROM @exchangePartition;  
					 EXECUTE stmt2;  
					 DEALLOCATE PREPARE stmt2; 
					

		-- 删除旧表分区
					SET @v_delete=concat('alter table pack_data_info drop PARTITION ',@query_del_name);
					PREPARE stmt3 from @v_delete; 
					EXECUTE stmt3;
					DEALLOCATE PREPARE stmt3;
				
		end if;
		-- 新增分区	 
			SELECT COUNT(partition_name) into name_num FROM INFORMATION_SCHEMA.partitions WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME='pack_data_info' and partition_name =@query_max_name;
			if name_num = 0 then					 
					SET @v_add=concat('ALTER TABLE pack_data_info ADD PARTITION (PARTITION p',@now_name,' VALUES LESS THAN (TO_DAYS(\'',@Max_date,'\')))');					
					PREPARE stmt4 from @v_add;				
					EXECUTE stmt4;
					DEALLOCATE PREPARE stmt4;	
						
			end if;

END$$

DELIMITER ;  

#2018.1.22 分区的事件

DELIMITER $$

ALTER DEFINER=`amplifi`@`localhost` EVENT `pack_event` ON SCHEDULE EVERY 1 MONTH STARTS '2017-12-05 00:00:00' ON COMPLETION PRESERVE ENABLE DO CALL pack_date_info_partition()$$

DELIMITER ;
       
#2018.1.23
INSERT INTO `parameters` (`parameter_code`, `parameter_value`, `parameter_desc`, `parameter_category`) VALUES ('resistance ', '3', '内阻', '2')   


 #2018.1.24
create SCHEMA backup_station_management;

#2018.1.24
DELIMITER $$

DROP PROCEDURE IF EXISTS `base_station_management`.`pack_date_info_partition`$$

CREATE DEFINER=`amplifi`@`%` PROCEDURE `pack_date_info_partition`()
BEGIN
		DECLARE pdate DATE;  		
		DECLARE num int DEFAULT 0; 
		DECLARE name_num int DEFAULT 1; 
			set pdate =DATE_FORMAT(now(),'%Y-%m-01');
			set @Max_date = (date_add(pdate, interval 2 MONTH));
			set @Max_name = (date_add(pdate, interval 1 MONTH)); 
			set @now_year = DATE_FORMAT(@Max_name,'%Y');
			set @now_month = DATE_FORMAT(@Max_name,'%m');
			SET @now_name= concat(@now_year,'',@now_month);
			set @query_max_name = concat('p',@now_name); -- 最大分区的名称
			set @del_date = (date_sub(pdate, interval 4 MONTH));
			set @del_year = DATE_FORMAT(@del_date,'%Y');
			set @del_month = DATE_FORMAT(@del_date,'%m');
			SET @del_name= concat(@del_year,'',@del_month);
			set @query_del_name = concat('p',@del_name); -- 删除，备份分区的名称
		 SELECT COUNT(partition_name) into num FROM INFORMATION_SCHEMA.partitions WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME='pack_data_info' and partition_name =@query_del_name;
			-- select partition_name into @p_name from INFORMATION_SCHEMA.PARTITIONS where  table_schema = SCHEMA() and table_name="pack_data_info" and partition_name =@quer_del_name;	
		
		IF num = 1 THEN 	
			-- 备份表
					set @back=concat('create table backup_station_management.pack_data_info_',@query_del_name,' like pack_data_info');   
					 PREPARE stmt1 FROM @back;  
					 EXECUTE stmt1;  
					 DEALLOCATE PREPARE stmt1; 				
						
				-- 删除新表分区	
					set @deleNewPartition=concat('alter table backup_station_management.pack_data_info_',@query_del_name,' REMOVE PARTITIONING');			
					 PREPARE stmt1 FROM @deleNewPartition;  
					 EXECUTE stmt1;  
					 DEALLOCATE PREPARE stmt1; 
						
		-- 交换分区
					set @exchangePartition=concat('alter table pack_data_info exchange partition ',@query_del_name,' with table backup_station_management.pack_data_info_',@query_del_name);		
					 PREPARE stmt2 FROM @exchangePartition;  
					 EXECUTE stmt2;  
					 DEALLOCATE PREPARE stmt2; 
					
		-- 删除旧表分区
					SET @v_delete=concat('alter table pack_data_info drop PARTITION ',@query_del_name);
					PREPARE stmt3 from @v_delete; 
					EXECUTE stmt3;
					DEALLOCATE PREPARE stmt3;
				
		end if;
		-- 新增分区	 
			SELECT COUNT(partition_name) into name_num FROM INFORMATION_SCHEMA.partitions WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME='pack_data_info' and partition_name =@query_max_name;
			if name_num = 0 then					 
					SET @v_add=concat('ALTER TABLE pack_data_info ADD PARTITION (PARTITION p',@now_name,' VALUES LESS THAN (TO_DAYS(\'',@Max_date,'\')))');					
					PREPARE stmt4 from @v_add;				
					EXECUTE stmt4;
					DEALLOCATE PREPARE stmt4;	
						
			end if;
END$$

DELIMITER ;    
       
#2018-1-24
CREATE TABLE `base_station_management`.`discharge_abstract_record` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `gprs_id` VARCHAR(12) NULL,
  `starttime` DATETIME NULL COMMENT '放电开始时间',
  `endtime` DATETIME NULL COMMENT '放电结束时间',
  `start_vol` DECIMAL(6,3) NULL COMMENT '放电开始电压（取前5放电电压平均值）',
  `end_vol` DECIMAL(6,3) NULL COMMENT '放电结束电压（取最后5个放电电压平均值）',
  `record_start_id` INT NULL COMMENT '放电开始id,pack_data_info里面的id',
  `record_end_id` INT NULL COMMENT '放电结束id,pack_data_info里面的id',
  PRIMARY KEY (`id`))
COMMENT = '放电摘要记录';

#2018.1.25
ALTER table gprs_config_info add column bad_cell_resist DECIMAL (10,4) null default null comment '异常内阻值';

alter table base_station_info add column load_current decimal(10,4) null default null comment '负载电流' ;
#2018.1.26
alter table discharge_abstract_record add column gen_cur decimal (6.3) default null comment '总电流';
alter table discharge_abstract_record add column discharge_time decimal(6.3) default null comment '放电时长';
alter table gprs_config_info add column check_discharge_time decimal(6.3) default null comment '核容量放电时长';
alter table gprs_config_info  add column  check_discharge_vol decimal(6.3) default null comment '核容电压';

#2018/1/26  
ALTER 
ALGORITHM=UNDEFINED 
DEFINER=`amplifi`@`%` 
SQL SECURITY DEFINER 
VIEW `full_station_info` AS 
SELECT
	`st`.`id` AS `id`,
	`st`.`gprs_id` AS `gprs_id`,
	`st`.`gprs_id_out` AS `gprs_id_out`,
	`st`.`name` AS `name`,
	`st`.`address` AS `address`,
	`st`.`province` AS `province`,
	`st`.`city` AS `city`,
	`st`.`district` AS `district`,
	`st`.`lat` AS `lat`,
	`st`.`lng` AS `lng`,
	`st`.`maintainance_id` AS `maintainance_id`,
	`st`.`pack_type` AS `pack_type`,
	`st`.`room_type` AS `room_type`,
	`st`.`duration` AS `duration`,
	`st`.`real_duration` AS `real_duration`,
	`st`.`ok_num` AS `ok_num`,
	`st`.`poor_num` AS `poor_num`,
	`st`.`error_num` AS `error_num`,
	`st`.`status` AS `status`,
	`st`.`company_id1` AS `company_id1`,
	`st`.`company_id2` AS `company_id2`,
	`st`.`company_id3` AS `company_id3`,
	`st`.`del_flag` AS `del_flag`,
	`st`.`company_name3` AS `company_name3`,
	`st`.`vol_level` AS `vol_level`,
	`st`.`operator_type` AS `operator_type`,
	`st`.`duration_status` AS `duration_status`,
	`st`.`update_time` AS `update_time`,
	`st`.`load_power` AS `load_power`,
	`st`.`inspect_status` AS `inspect_status`,
	`st`.`address_coding` AS `address_coding`,
	`st`.`cell_count` AS `cell_count`,
  `st`.`load_current` AS `load_current`,
	`config`.`link_status` AS `link_status`,
	`config`.`device_type` AS `device_type`,
	`pack`.`state` AS `state`,
	`pack`.`gen_vol` AS `gen_vol`,
	`pack`.`gen_cur` AS `gen_cur`
FROM
	(
		(
			`base_station_info` `st`
			LEFT JOIN `gprs_config_info` `config` ON (
				(
					`st`.`gprs_id` = `config`.`gprs_id`
				)
			)
		)
		LEFT JOIN `pack_data_info_latest` `pack` ON (
			(
				`config`.`gprs_id` = `pack`.`gprs_id`
			)
		)
	) ;



#2018.1.29
alter table sug_replace_cell_index add column remark varchar(252)null default null comment '整治备注'


#2018.1.30
ALTER TABLE gprs_config_info alter column bad_cell_resist drop default;
ALTER TABLE gprs_config_info alter column bad_cell_resist set default 6; 

ALTER TABLE gprs_config_info alter column check_discharge_time drop default;
ALTER TABLE gprs_config_info alter column check_discharge_time set default 3; 

ALTER TABLE gprs_config_info alter column check_discharge_vol drop default;
ALTER TABLE gprs_config_info alter column check_discharge_vol set default 47; 

#2018.1.30
ALTER TABLE `base_station_management`.`cell_info` 
ADD COLUMN `fault_mark` INT(2) NULL DEFAULT 0 COMMENT '巡检记录中标记单体故障标识。0，正常，1，故障，默认为0.' AFTER `pulse_send_done`;


#2018.1.31
alter table discharge_abstract_record add column autoBalance tinyint(1) null default null comment '是否自动均衡';
INSERT INTO `parameters` (`parameter_code`, `parameter_category`, `parameter_value`, `parameter_desc`) VALUES ('check_discharge_time', '1', '47', '核容放电时长');
INSERT INTO `parameters` (`parameter_code`, `parameter_category`, `parameter_value`, `parameter_desc`) VALUES ('check_discharge_vol', '1', '47', '核容电压');

ALTER TABLE `base_station_management`.`gprs_config_info` 
CHANGE COLUMN `gprs_flag` `gprs_flag` INT(11) NULL DEFAULT '0' COMMENT '主机类型标志1 备用主机0配套主机' ,
CHANGE COLUMN `bad_cell_resist` `bad_cell_resist` DECIMAL(10,4) NULL DEFAULT 10 COMMENT '异常内阻值' ,
CHANGE COLUMN `check_discharge_time` `check_discharge_time` DECIMAL(6,3) NULL DEFAULT 3.5 COMMENT '核容量放电时长' ,
CHANGE COLUMN `check_discharge_vol` `check_discharge_vol` DECIMAL(6,0) NULL DEFAULT 47 COMMENT '核容电压' ;

alter table discharge_abstract_record add column newCellCount int(11) null default null comment '单体数量';

#2018.2.2
alter table discharge_abstract_record add column balanceStartTime datetime null default null comment '均衡开始时间';

alter table discharge_abstract_record add column balanceEndTime datetime null default null comment '均衡结束时间';
 #2018.2.2
ALTER TABLE `discharge_abstract_record`
MODIFY COLUMN `discharge_time`  decimal(6,3) NULL DEFAULT NULL COMMENT '放电时长' AFTER `gen_cur`;

#2018.3.5
ALTER TABLE `gprs_config_info`
ADD COLUMN `server_num` int(11) NOT NULL DEFAULT '0' AFTER `check_discharge_vol`,
ADD COLUMN `current_warning_toplimit` int(11) NOT NULL DEFAULT '0' COMMENT '电流异常告警上限' AFTER `server_num`,
ADD COLUMN `current_warning_lowerlimit` int(11) NOT NULL DEFAULT '0' COMMENT '电流异常告警下限' AFTER `current_warning_toplimit`,
ADD COLUMN `single_high_vol_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '单体过压告警阈值' AFTER `current_warning_lowerlimit`,
ADD COLUMN `single_low_vol_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '单体欠压告警阈值' AFTER `single_high_vol_threshold`,
ADD COLUMN `high_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '总电压过压恢复点' AFTER `single_low_vol_threshold`,
ADD COLUMN `low_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '总电压欠压恢复点' AFTER `high_vol_recover`,
ADD COLUMN `single_high_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体过压恢复点' AFTER `low_vol_recover`,
ADD COLUMN `single_low_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体欠压恢复点' AFTER `single_high_vol_recover`,
ADD COLUMN `high_surroundingtem_warning_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '环境高温告警阈值' AFTER `single_low_vol_recover`,
ADD COLUMN `low_surroundingtem_warning_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '环境低温告警阈值' AFTER `high_surroundingtem_warning_threshold`,
ADD COLUMN `high_surroundingtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '环境高温告警恢复点' AFTER `low_surroundingtem_warning_threshold`,
ADD COLUMN `low_surroundingtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '环境低温告警恢复点' AFTER `high_surroundingtem_warning_recover`,
ADD COLUMN `hightem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体高温告警恢复点' AFTER `low_surroundingtem_warning_recover`,
ADD COLUMN `lowtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体低温告警恢复点' AFTER `hightem_warning_recover`,
ADD COLUMN `low_soc_recover` int(11) NOT NULL DEFAULT '0' COMMENT 'soc过低' AFTER `lowtem_warning_recover`;
#2018.3.5
ALTER TABLE `gprs_config_send`
ADD COLUMN `current_warning_toplimit` int(11) NOT NULL DEFAULT '0' COMMENT '电流异常告警上限' AFTER `send_done`,
ADD COLUMN `current_warning_lowerlimit` int(11) NOT NULL DEFAULT '0' COMMENT '电流异常告警下限' AFTER `current_warning_toplimit`,
ADD COLUMN `single_high_vol_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '单体过压告警阈值' AFTER `current_warning_lowerlimit`,
ADD COLUMN `single_low_vol_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '单体欠压告警阈值' AFTER `single_high_vol_threshold`,
ADD COLUMN `high_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '总电压过压恢复点' AFTER `single_low_vol_threshold`,
ADD COLUMN `low_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '总电压欠压恢复点' AFTER `high_vol_recover`,
ADD COLUMN `single_high_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体过压恢复点' AFTER `low_vol_recover`,
ADD COLUMN `single_low_vol_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体欠压恢复点' AFTER `single_high_vol_recover`,
ADD COLUMN `high_surroundingtem_warning_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '环境高温告警阈值' AFTER `single_low_vol_recover`,
ADD COLUMN `low_surroundingtem_warning_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '环境低温告警阈值' AFTER `high_surroundingtem_warning_threshold`,
ADD COLUMN `high_surroundingtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '环境高温告警恢复点' AFTER `low_surroundingtem_warning_threshold`,
ADD COLUMN `low_surroundingtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '环境低温告警恢复点' AFTER `high_surroundingtem_warning_recover`,
ADD COLUMN `hightem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体高温告警恢复点' AFTER `low_surroundingtem_warning_recover`,
ADD COLUMN `lowtem_warning_recover` int(11) NOT NULL DEFAULT '0' COMMENT '单体低温告警恢复点' AFTER `hightem_warning_recover`,
ADD COLUMN `low_soc_recover` int(11) NOT NULL DEFAULT '0' COMMENT 'soc过低' AFTER `lowtem_warning_recover`;

#2018.3.5
create SCHEMA backup_station_management -- 创建备份表存储库

#2018.3.6
alter table base_station_management.warning_info
add column abnormal_current tinyint not null default 0 comment '异常电流' ,
add column single_vol_high tinyint not null default 0 comment '单体电压过高',
add column single_vol_low tinyint not null default 0 comment '单体电压过低';

alter table base_station_management.warning_info_latest
add column abnormal_current tinyint not null default 0 comment '异常电流' ,
add column single_vol_high tinyint not null default 0 comment '单体电压过高',
add column single_vol_low tinyint not null default 0 comment '单体电压过低';

#2018.3.7
ALTER TABLE `gprs_config_info`
MODIFY COLUMN `vol_high_warning_threshold`  decimal(6,3) NULL DEFAULT NULL COMMENT '总电压过高告警阈值,V' AFTER `current_capacity`,
MODIFY COLUMN `vol_low_warning_threshold`  decimal(6,3) NULL DEFAULT NULL COMMENT '总电压过低告警阈值 V' AFTER `vol_high_warning_threshold`,
MODIFY COLUMN `current_warning_toplimit`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '电流异常告警上限' AFTER `server_num`,
MODIFY COLUMN `current_warning_lowerlimit`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '电流异常告警下限' AFTER `current_warning_toplimit`,
MODIFY COLUMN `single_high_vol_threshold`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体过压告警阈值' AFTER `current_warning_lowerlimit`,
MODIFY COLUMN `single_low_vol_threshold`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体欠压告警阈值' AFTER `single_high_vol_threshold`,
MODIFY COLUMN `high_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '总电压过压恢复点' AFTER `single_low_vol_threshold`,
MODIFY COLUMN `low_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '总电压欠压恢复点' AFTER `high_vol_recover`,
MODIFY COLUMN `single_high_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体过压恢复点' AFTER `low_vol_recover`,
MODIFY COLUMN `single_low_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体欠压恢复点' AFTER `single_high_vol_recover`;
#2018.3.7
ALTER TABLE `gprs_config_send`
MODIFY COLUMN `vol_high_warning_threshold`  decimal(6,3) NULL DEFAULT NULL COMMENT '总电压过高告警阈值,V' AFTER `current_capacity`,
MODIFY COLUMN `vol_low_warning_threshold`  decimal(6,3) NULL DEFAULT NULL COMMENT '总电压过低告警阈值 V' AFTER `vol_high_warning_threshold`,
MODIFY COLUMN `current_warning_toplimit`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '电流异常告警上限' AFTER `send_done`,
MODIFY COLUMN `current_warning_lowerlimit`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '电流异常告警下限' AFTER `current_warning_toplimit`,
MODIFY COLUMN `single_high_vol_threshold`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体过压告警阈值' AFTER `current_warning_lowerlimit`,
MODIFY COLUMN `single_low_vol_threshold`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体欠压告警阈值' AFTER `single_high_vol_threshold`,
MODIFY COLUMN `high_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '总电压过压恢复点' AFTER `single_low_vol_threshold`,
MODIFY COLUMN `low_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '总电压欠压恢复点' AFTER `high_vol_recover`,
MODIFY COLUMN `single_high_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体过压恢复点' AFTER `low_vol_recover`,
MODIFY COLUMN `single_low_vol_recover`  decimal(6,3) NOT NULL DEFAULT 0 COMMENT '单体欠压恢复点' AFTER `single_high_vol_recover`;


#2018.3.8
alter table gprs_config_info alter column  check_discharge_vol set default 47.0; 

ALTER TABLE `base_station_management`.`gprs_config_info` 
CHANGE COLUMN `current_warning_toplimit` `current_warning_toplimit` DECIMAL(9,3) NULL COMMENT '电流异常告警上限' ,
CHANGE COLUMN `current_warning_lowerlimit` `current_warning_lowerlimit` DECIMAL(9,3) NULL COMMENT '电流异常告警下限' ,
CHANGE COLUMN `single_high_vol_threshold` `single_high_vol_threshold` DECIMAL(9,3) NULL COMMENT '单体过压告警阈值' ,
CHANGE COLUMN `single_low_vol_threshold` `single_low_vol_threshold` DECIMAL(9,3) NULL COMMENT '单体欠压告警阈值' ,
CHANGE COLUMN `high_vol_recover` `high_vol_recover` DECIMAL(9,3) NULL COMMENT '总电压过压恢复点' ,
CHANGE COLUMN `low_vol_recover` `low_vol_recover` DECIMAL(9,3) NULL COMMENT '总电压欠压恢复点' ,
CHANGE COLUMN `single_high_vol_recover` `single_high_vol_recover` DECIMAL(9,3) NULL COMMENT '单体过压恢复点' ,
CHANGE COLUMN `single_low_vol_recover` `single_low_vol_recover` DECIMAL(9,3) NULL COMMENT '单体欠压恢复点' ,
CHANGE COLUMN `high_surroundingtem_warning_threshold` `high_surroundingtem_warning_threshold` INT(11) NULL COMMENT '环境高温告警阈值' ,
CHANGE COLUMN `low_surroundingtem_warning_threshold` `low_surroundingtem_warning_threshold` INT(11) NULL COMMENT '环境低温告警阈值' ,
CHANGE COLUMN `high_surroundingtem_warning_recover` `high_surroundingtem_warning_recover` INT(11) NULL COMMENT '环境高温告警恢复点' ,
CHANGE COLUMN `low_surroundingtem_warning_recover` `low_surroundingtem_warning_recover` INT(11) NULL COMMENT '环境低温告警恢复点' ,
CHANGE COLUMN `hightem_warning_recover` `hightem_warning_recover` INT(11) NULL COMMENT '单体高温告警恢复点' ,
CHANGE COLUMN `lowtem_warning_recover` `lowtem_warning_recover` INT(11) NULL COMMENT '单体低温告警恢复点' ,
CHANGE COLUMN `low_soc_recover` `low_soc_recover` INT(11) NULL COMMENT 'soc过低' ;

#2018.3.12
alter table cell_info add column  mark_time datetime DEFAULT null COMMENT '故障标记时间'

#2018.3.14
alter table sug_replace_cell_index  add column  discharge_start_vol decimal(6,3) default null comment '放电开始电压';
alter table sug_replace_cell_index  add column  discharge_end_vol decimal(6,3) default null comment '放电结束电压';
alter table sug_replace_cell_index add column  discharge_time decimal(6,3) DEFAULT null COMMENT '放电时长';
alter table sug_replace_cell_index add column  discharge_start_time datetime DEFAULT null COMMENT '放电开始时间';
alter table sug_replace_cell_index add column  discharge_end_time datetime DEFAULT null COMMENT '放电结束时间';
alter table sug_replace_cell_index add column  load_current decimal(10,3) DEFAULT null COMMENT '负载电流';
alter table sug_replace_cell_index add column  replace_num int(11) DEFAULT null COMMENT '跟换电池数量';
alter table sug_replace_cell_index add column  sign_cells varchar(255) DEFAULT null COMMENT '巡检标记单体';
alter table sug_replace_cell_index add column  resist_high_cells varchar(255) DEFAULT null COMMENT '内阻过大单体';
alter table sug_replace_cell_index add column  rectification_mode varchar(255) DEFAULT null COMMENT '整治方式';
alter table sug_replace_cell_index add column  discharge_abstract_record_id int(11)  DEFAULT null COMMENT '放电摘要表id';
#2018.03.16
ALTER TABLE `sug_replace_cell_index`
MODIFY COLUMN `discharge_start_time`  varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '放电开始时间' AFTER `discharge_end_vol`,
MODIFY COLUMN `discharge_end_time`  varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '放电结束时间' AFTER `discharge_start_time`;


#2018.03.20
INSERT INTO `parameters` (`parameter_code`, `parameter_category`, `parameter_value`, `parameter_desc`) VALUES ('staticVol', '1', '50', '静态电压');
INSERT INTO `parameters` (`parameter_code`, `parameter_category`, `parameter_value`, `parameter_desc`) VALUES ('staticMinCur', '1', '50', '静态最小电流');
INSERT INTO `parameters` (`parameter_code`, `parameter_category`, `parameter_value`, `parameter_desc`) VALUES ('staticMaxCur', '1', '90', '静态最大电流');

#2018.03.30
alter table pulse_discharge_info  MODIFY column  impendance decimal(11,3) default null comment '电池内阻，单位毫欧';

ALTER TABLE `pack_data_expand_latest`
MODIFY COLUMN `cell_resist_1`  decimal(12,4) NULL DEFAULT NULL COMMENT '1号电池内阻' AFTER `pack_discharge_time_pred`,
MODIFY COLUMN `cell_resist_2`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_1`,
MODIFY COLUMN `cell_resist_3`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_2`,
MODIFY COLUMN `cell_resist_4`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_3`,
MODIFY COLUMN `cell_resist_5`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_4`,
MODIFY COLUMN `cell_resist_6`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_5`,
MODIFY COLUMN `cell_resist_7`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_6`,
MODIFY COLUMN `cell_resist_8`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_7`,
MODIFY COLUMN `cell_resist_9`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_8`,
MODIFY COLUMN `cell_resist_10`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_9`,
MODIFY COLUMN `cell_resist_11`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_10`,
MODIFY COLUMN `cell_resist_12`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_11`,
MODIFY COLUMN `cell_resist_13`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_12`,
MODIFY COLUMN `cell_resist_14`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_13`,
MODIFY COLUMN `cell_resist_15`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_14`,
MODIFY COLUMN `cell_resist_16`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_15`,
MODIFY COLUMN `cell_resist_17`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_16`,
MODIFY COLUMN `cell_resist_18`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_17`,
MODIFY COLUMN `cell_resist_19`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_18`,
MODIFY COLUMN `cell_resist_20`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_19`,
MODIFY COLUMN `cell_resist_21`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_20`,
MODIFY COLUMN `cell_resist_22`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_21`,
MODIFY COLUMN `cell_resist_23`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_22`,
MODIFY COLUMN `cell_resist_24`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_23`;


ALTER TABLE `pack_data_expand`
MODIFY COLUMN `cell_resist_1`  decimal(12,4) NULL DEFAULT NULL COMMENT '1号电池内阻' AFTER `pack_discharge_time_pred`,
MODIFY COLUMN `cell_resist_2`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_1`,
MODIFY COLUMN `cell_resist_3`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_2`,
MODIFY COLUMN `cell_resist_4`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_3`,
MODIFY COLUMN `cell_resist_5`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_4`,
MODIFY COLUMN `cell_resist_6`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_5`,
MODIFY COLUMN `cell_resist_7`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_6`,
MODIFY COLUMN `cell_resist_8`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_7`,
MODIFY COLUMN `cell_resist_9`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_8`,
MODIFY COLUMN `cell_resist_10`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_9`,
MODIFY COLUMN `cell_resist_11`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_10`,
MODIFY COLUMN `cell_resist_12`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_11`,
MODIFY COLUMN `cell_resist_13`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_12`,
MODIFY COLUMN `cell_resist_14`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_13`,
MODIFY COLUMN `cell_resist_15`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_14`,
MODIFY COLUMN `cell_resist_16`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_15`,
MODIFY COLUMN `cell_resist_17`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_16`,
MODIFY COLUMN `cell_resist_18`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_17`,
MODIFY COLUMN `cell_resist_19`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_18`,
MODIFY COLUMN `cell_resist_20`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_19`,
MODIFY COLUMN `cell_resist_21`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_20`,
MODIFY COLUMN `cell_resist_22`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_21`,
MODIFY COLUMN `cell_resist_23`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_22`,
MODIFY COLUMN `cell_resist_24`  decimal(12,4) NULL DEFAULT NULL AFTER `cell_resist_23`;

#2018.4.12
CREATE TABLE `discharge_manual_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `district` varchar(45) DEFAULT NULL COMMENT '区域',
  `address_coding` varchar(32) DEFAULT NULL COMMENT '站址编码',
  `maintainance_id` varchar(45) DEFAULT NULL COMMENT '运维id',
  `address_name` varchar(45) DEFAULT NULL COMMENT '站名',
  `gprs_id` varchar(32) DEFAULT NULL COMMENT '设备id',
  `discharge_start_time` datetime DEFAULT NULL COMMENT '开始放电时间',
  `discharge_end_time` datetime DEFAULT NULL COMMENT '放电结束时间',
  `discharge_forword_vol` decimal(6,3) DEFAULT NULL COMMENT '放电前电压',
  `discharge_forword_cur` decimal(6,3) DEFAULT NULL COMMENT '放电前电流',
  `discharge_forword_system_cur` decimal(6,3) DEFAULT NULL COMMENT '放电前系统电流',
  `discharge_forword_system_vol` decimal(6,3) DEFAULT NULL COMMENT '放电前系统电压',
  `discharge_back_vol` decimal(6,3) DEFAULT NULL COMMENT '放电截止电压',
  `discharge_back_cur` decimal(6,3) DEFAULT NULL COMMENT '放电截止电流',
  `discharge_back_system_cur` decimal(6,3) DEFAULT NULL COMMENT '放电截止系统电流',
  `discharge_back_system_vol` decimal(6,3) DEFAULT NULL COMMENT '放电截止系统电压',
  `discharge_time` decimal(6,3) DEFAULT NULL COMMENT '放电时长',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `install_time` datetime DEFAULT NULL COMMENT '安装时间',
  `cell_plant` varchar(32) DEFAULT NULL COMMENT '电池品牌',
  `cell_type` varchar(45) DEFAULT NULL COMMENT '电池类型',
  `report_history` varchar(32) DEFAULT NULL COMMENT '出过历史整治报告',
  `is_processed` int(5) DEFAULT NULL COMMENT '是否出具整治报告，1是，0否',
  `report_file_name` varchar(45) DEFAULT NULL COMMENT '报告文件名称',
  `report_remark` varchar(45) DEFAULT NULL COMMENT '整治备注',
  `company_name3` varchar(45) DEFAULT NULL COMMENT '三级公司名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='放电手工记录';

#2018-4-16
#将电池整治策略修改为电池整治建议报表
UPDATE `permissions` SET `permission_name`='电池整治建议报表' WHERE (`permission_id`='21')
#2018-4-16
ALTER TABLE `discharge_manual_record`
ADD COLUMN `discharge_person`  varchar(32) NULL COMMENT '放电人员' AFTER `company_name3`;



























