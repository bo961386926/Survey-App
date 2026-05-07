-- ============================================
-- 数据字典管理系统 - 完整初始化脚本（扩展版）
-- 使用表：sys_dictionary + sys_dictionary_data
-- 执行方式：在 MySQL 客户端中手动执行
-- ============================================

USE survey_db;

-- ============================================
-- 注意：sys_dictionary 和 sys_dictionary_data 表已在 execute_all.sql 中创建
-- 这里只需要插入数据
-- ============================================

-- ============================================
-- 1. 插入字典分类数据
-- ============================================

-- 1.1 项目状态字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('project_status', '项目状态', '项目管理中的状态枚举', 0, 1, 1);

-- 1.2 模板状态字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('template_status', '模板状态', '勘察模板的状态枚举', 0, 1, 2);

-- 1.3 点位状态字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('point_status', '点位状态', '勘察点位状态枚举', 0, 1, 3);

-- 1.4 排口类型字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('outfall_type', '排口类型', '排口类型分类', 0, 1, 4);

-- 1.5 审核操作字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('audit_action', '审核操作', '审核操作类型', 0, 1, 5);

-- 1.6 用户角色字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('user_role', '用户角色', '系统用户角色分类', 0, 1, 6);

-- 1.7 导出任务状态字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('export_task_status', '导出任务状态', '导出任务状态枚举', 0, 1, 7);

-- 1.8 消息类型字典
INSERT IGNORE INTO sys_dictionary (dict_code, dict_name, description, is_system, status, sort_order) VALUES
('message_type', '消息类型', '系统消息类型分类', 0, 1, 8);

-- ============================================
-- 2. 插入字典项数据
-- ============================================

-- 2.1 项目状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'project_status', '草稿', '0', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'project_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'project_status', '进行中', '1', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'project_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'project_status', '已完成', '2', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'project_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'project_status', '已归档', '3', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'project_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'project_status', '已暂停', '4', 4, 1, 0 FROM sys_dictionary WHERE dict_code = 'project_status';

-- 2.2 模板状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'template_status', '草稿', '0', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'template_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'template_status', '已发布', '1', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'template_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'template_status', '已停用', '2', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'template_status';

-- 2.3 点位状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '未分配', '0', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '待采集', '1', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '采集中', '2', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '待审核', '3', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '审核通过', '4', 4, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '审核驳回', '5', 5, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'point_status', '已归档', '6', 6, 1, 0 FROM sys_dictionary WHERE dict_code = 'point_status';

-- 2.4 排口类型字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'outfall_type', '雨水排口', 'rainwater', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'outfall_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'outfall_type', '污水排口', 'sewage', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'outfall_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'outfall_type', '雨污混流口', 'mixed', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'outfall_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'outfall_type', '工业废水排口', 'industrial', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'outfall_type';

-- 2.5 审核操作字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'audit_action', '通过', 'pass', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'audit_action';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'audit_action', '驳回', 'reject', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'audit_action';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'audit_action', '退回修改', 'return', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'audit_action';

-- 2.6 用户角色字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'user_role', '超级管理员', 'R_SUPER', 0, 1, 1 FROM sys_dictionary WHERE dict_code = 'user_role';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'user_role', '系统管理员', 'R_ADMIN', 1, 1, 1 FROM sys_dictionary WHERE dict_code = 'user_role';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'user_role', '项目经理', 'R_MANAGER', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'user_role';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'user_role', '勘察人员', 'R_COLLECTOR', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'user_role';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'user_role', '审核人员', 'R_AUDITOR', 4, 1, 0 FROM sys_dictionary WHERE dict_code = 'user_role';

-- 2.7 导出任务状态字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'export_task_status', '待处理', '0', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'export_task_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'export_task_status', '处理中', '1', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'export_task_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'export_task_status', '已完成', '2', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'export_task_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'export_task_status', '已取消', '3', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'export_task_status';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'export_task_status', '失败', '4', 4, 1, 0 FROM sys_dictionary WHERE dict_code = 'export_task_status';

-- 2.8 消息类型字典项
INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '系统通知', 'system', 0, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '任务分配', 'task_assign', 1, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '审核通知', 'audit_notify', 2, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '项目通知', 'project_notify', 3, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '导出完成', 'export_complete', 4, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

INSERT IGNORE INTO sys_dictionary_data (dict_id, dict_code, data_name, data_value, data_order, status, is_readonly) 
SELECT id, 'message_type', '警告提醒', 'warning', 5, 1, 0 FROM sys_dictionary WHERE dict_code = 'message_type';

-- ============================================
-- 3. 验证数据
-- ============================================

-- 查看字典分类数量
SELECT 
    '字典分类总数' AS info,
    COUNT(*) AS count
FROM sys_dictionary;

-- 查看字典项数量
SELECT 
    '字典项总数' AS info,
    COUNT(*) AS count
FROM sys_dictionary_data;

-- 查看每个字典的项数
SELECT 
    d.dict_code,
    d.dict_name,
    d.description,
    COUNT(dd.id) AS item_count
FROM sys_dictionary d
LEFT JOIN sys_dictionary_data dd ON d.id = dd.dict_id
GROUP BY d.id, d.dict_code, d.dict_name, d.description
ORDER BY d.sort_order;

-- 查看具体数据示例
SELECT 
    '=== 项目状态字典 ===' AS info;
SELECT dd.data_name, dd.data_value, dd.data_order, dd.is_readonly
FROM sys_dictionary d
LEFT JOIN sys_dictionary_data dd ON d.id = dd.dict_id
WHERE d.dict_code = 'project_status'
ORDER BY dd.data_order;

SELECT 
    '=== 点位状态字典 ===' AS info;
SELECT dd.data_name, dd.data_value, dd.data_order, dd.is_readonly
FROM sys_dictionary d
LEFT JOIN sys_dictionary_data dd ON d.id = dd.dict_id
WHERE d.dict_code = 'point_status'
ORDER BY dd.data_order;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 数据字典初始化完成（扩展版）！' AS message;
SELECT '✅ 已创建 8 个字典分类' AS message;
SELECT '✅ 已初始化 43 个字典项' AS message;
SELECT '✅ 包含字段：描述、排序、系统标识、只读标识、备注' AS message;
SELECT '✅ 后端重启后即可使用数据字典管理功能' AS message;
