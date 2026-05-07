-- 数据修复脚本
USE survey_db;

-- 插入点位状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='POINT_STATUS'), 'POINT_STATUS', '待采集', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='POINT_STATUS'), 'POINT_STATUS', '采集中', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='POINT_STATUS'), 'POINT_STATUS', '待审核', '2', 3, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='POINT_STATUS'), 'POINT_STATUS', '审核通过', '3', 4, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='POINT_STATUS'), 'POINT_STATUS', '审核驳回', '4', 5, 1);

-- 初始化审核状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='AUDIT_STATUS'), 'AUDIT_STATUS', '待审核', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='AUDIT_STATUS'), 'AUDIT_STATUS', '审核中', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='AUDIT_STATUS'), 'AUDIT_STATUS', '审核通过', '2', 3, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='AUDIT_STATUS'), 'AUDIT_STATUS', '审核驳回', '3', 4, 1);

-- 初始化结果状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='RESULT_STATUS'), 'RESULT_STATUS', '正常', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='RESULT_STATUS'), 'RESULT_STATUS', '异常', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='RESULT_STATUS'), 'RESULT_STATUS', '待确认', '2', 3, 1);

-- 初始化模板状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='TEMPLATE_STATUS'), 'TEMPLATE_STATUS', '草稿', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='TEMPLATE_STATUS'), 'TEMPLATE_STATUS', '已发布', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='TEMPLATE_STATUS'), 'TEMPLATE_STATUS', '已停用', '2', 3, 1);

-- 初始化是否标识字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='YES_NO'), 'YES_NO', '否', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='YES_NO'), 'YES_NO', '是', '1', 2, 1);

-- 初始化项目状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='PROJECT_STATUS'), 'PROJECT_STATUS', '草稿', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='PROJECT_STATUS'), 'PROJECT_STATUS', '进行中', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='PROJECT_STATUS'), 'PROJECT_STATUS', '已完成', '2', 3, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='PROJECT_STATUS'), 'PROJECT_STATUS', '已归档', '3', 4, 1);

-- 初始化标段状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, is_readonly) VALUES
((SELECT id FROM sys_dictionary WHERE dict_code='SECTION_STATUS'), 'SECTION_STATUS', '草稿', '0', 1, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='SECTION_STATUS'), 'SECTION_STATUS', '进行中', '1', 2, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='SECTION_STATUS'), 'SECTION_STATUS', '已完成', '2', 3, 1),
((SELECT id FROM sys_dictionary WHERE dict_code='SECTION_STATUS'), 'SECTION_STATUS', '已归档', '3', 4, 1);

-- 验证结果
SELECT '数据修复完成！' AS status;
SELECT 'sys_dictionary表:' AS info;
SELECT * FROM sys_dictionary;
SELECT 'sys_dictionary_data表:' AS info;
SELECT * FROM sys_dictionary_data;
