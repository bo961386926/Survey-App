b执行-- 数据字典分类表
CREATE TABLE IF NOT EXISTS `sys_dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典代码（唯一标识）',
  `dict_name` VARCHAR(128) NOT NULL COMMENT '字典名称',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '字典描述',
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置：0否 1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典分类表';

-- 数据字典项表
CREATE TABLE IF NOT EXISTS `sys_dictionary_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
  `dict_id` BIGINT NOT NULL COMMENT '字典分类ID',
  `dict_code` VARCHAR(64) NOT NULL COMMENT '字典代码（冗余字段，便于查询）',
  `data_name` VARCHAR(128) NOT NULL COMMENT '字典项名称',
  `data_value` VARCHAR(128) NOT NULL COMMENT '字典项值',
  `data_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `is_readonly` TINYINT NOT NULL DEFAULT 0 COMMENT '是否只读：0否 1是',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dict_code` (`dict_code`),
  KEY `idx_dict_id` (`dict_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- 初始化系统内置字典分类
INSERT INTO `sys_dictionary` (`dict_code`, `dict_name`, `description`, `is_system`, `status`, `sort_order`) VALUES
('POINT_STATUS', '点位状态', '勘察点位状态字典', 1, 1, 1),
('AUDIT_STATUS', '审核状态', '审核流程状态字典', 1, 1, 2),
('RESULT_STATUS', '结果状态', '勘察结果状态字典', 1, 1, 3),
('TEMPLATE_STATUS', '模板状态', '勘察模板状态字典', 1, 1, 4),
('YES_NO', '是否标识', '通用是否标识字典', 1, 1, 5),
('PROJECT_STATUS', '项目状态', '项目状态字典', 1, 1, 6),
('SECTION_STATUS', '标段状态', '标段状态字典', 1, 1, 7);

-- 初始化点位状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(1, 'POINT_STATUS', '待采集', '0', 1, 1),
(1, 'POINT_STATUS', '采集中', '1', 2, 1),
(1, 'POINT_STATUS', '待审核', '2', 3, 1),
(1, 'POINT_STATUS', '审核通过', '3', 4, 1),
(1, 'POINT_STATUS', '审核驳回', '4', 5, 1);

-- 初始化审核状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(2, 'AUDIT_STATUS', '待审核', '0', 1, 1),
(2, 'AUDIT_STATUS', '审核中', '1', 2, 1),
(2, 'AUDIT_STATUS', '审核通过', '2', 3, 1),
(2, 'AUDIT_STATUS', '审核驳回', '3', 4, 1);

-- 初始化结果状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(3, 'RESULT_STATUS', '正常', '0', 1, 1),
(3, 'RESULT_STATUS', '异常', '1', 2, 1),
(3, 'RESULT_STATUS', '待确认', '2', 3, 1);

-- 初始化模板状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(4, 'TEMPLATE_STATUS', '草稿', '0', 1, 1),
(4, 'TEMPLATE_STATUS', '已发布', '1', 2, 1),
(4, 'TEMPLATE_STATUS', '已停用', '2', 3, 1);

-- 初始化是否标识字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(5, 'YES_NO', '否', '0', 1, 1),
(5, 'YES_NO', '是', '1', 2, 1);

-- 初始化项目状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(6, 'PROJECT_STATUS', '草稿', '0', 1, 1),
(6, 'PROJECT_STATUS', '进行中', '1', 2, 1),
(6, 'PROJECT_STATUS', '已完成', '2', 3, 1),
(6, 'PROJECT_STATUS', '已归档', '3', 4, 1);

-- 初始化标段状态字典项
INSERT INTO `sys_dictionary_data` (`dict_id`, `dict_code`, `data_name`, `data_value`, `data_order`, `is_readonly`) VALUES
(7, 'SECTION_STATUS', '草稿', '0', 1, 1),
(7, 'SECTION_STATUS', '进行中', '1', 2, 1),
(7, 'SECTION_STATUS', '已完成', '2', 3, 1),
(7, 'SECTION_STATUS', '已归档', '3', 4, 1);