-- =============================================
-- 数据字典初始化数据
-- 执行时间: 2026-05-06
-- =============================================

USE survey_db;

-- =============================================
-- 1. 项目状态字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('project_status', '项目状态', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '草稿', '0', 0, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '进行中', '1', 1, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已暂停', '2', 2, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已完成', '3', 3, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已归档', '4', 4, 1 FROM sys_dict WHERE dict_code = 'project_status';

-- =============================================
-- 2. 模板状态字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('template_status', '模板状态', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '草稿', '0', 0, 1 FROM sys_dict WHERE dict_code = 'template_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已发布', '1', 1, 1 FROM sys_dict WHERE dict_code = 'template_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已停用', '2', 2, 1 FROM sys_dict WHERE dict_code = 'template_status';

-- =============================================
-- 3. 点位状态字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('point_status', '点位状态', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待采集', '0', 0, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '草稿中', '1', 1, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待审核', '2', 2, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核通过', '3', 3, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '驳回待修改', '4', 4, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已归档', '5', 5, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '作废', '6', 6, 1 FROM sys_dict WHERE dict_code = 'point_status';

-- =============================================
-- 4. 排口类型字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('outfall_type', '排口类型', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '雨水排口', 'rain', 0, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '污水排口', 'sewage', 1, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '雨污混流口', 'mixed', 2, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '工业废水排口', 'industrial', 3, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

-- =============================================
-- 5. 审核操作字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('audit_action', '审核操作', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '通过', 'pass', 0, 1 FROM sys_dict WHERE dict_code = 'audit_action';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '驳回', 'reject', 1, 1 FROM sys_dict WHERE dict_code = 'audit_action';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '转交', 'transfer', 2, 1 FROM sys_dict WHERE dict_code = 'audit_action';

-- =============================================
-- 6. 用户角色字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('user_role', '用户角色', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '超级管理员', 'SUPER_ADMIN', 0, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '管理员', 'ADMIN', 1, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '项目经理', 'PROJECT_MANAGER', 2, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '勘察员', 'SURVEYOR', 3, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核员', 'AUDITOR', 4, 1 FROM sys_dict WHERE dict_code = 'user_role';

-- =============================================
-- 7. 导出任务状态字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('export_task_status', '导出任务状态', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待生成', '0', 0, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '生成中', '1', 1, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已完成', '2', 2, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '失败', '3', 3, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已过期', '4', 4, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

-- =============================================
-- 8. 消息类型字典
-- =============================================
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('message_type', '消息类型', 1);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核提醒', 'audit_reminder', 0, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '项目延期', 'project_delay', 1, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '模板发布', 'template_publish', 2, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '导出完成', 'export_complete', 3, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '协作过期', 'collab_expire', 4, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '风险预警', 'risk_alert', 5, 1 FROM sys_dict WHERE dict_code = 'message_type';

-- =============================================
-- 验证结果
-- =============================================
SELECT '数据字典初始化完成！' AS status;
SELECT COUNT(*) AS '字典数量' FROM sys_dict;
SELECT COUNT(*) AS '字典项数量' FROM sys_dict_item;
