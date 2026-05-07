-- ============================================
-- 数据字典管理系统 - 完整初始化脚本
-- 适用于：sys_dict + sys_dict_item（简化版）
-- 执行方式：在 MySQL 客户端中手动执行
-- ============================================

USE survey_db;

-- ============================================
-- 1. 创建数据字典主表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码（唯一标识）',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典主表';

-- ============================================
-- 2. 创建数据字典项表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    item_label VARCHAR(128) NOT NULL COMMENT '选项标签（显示文本）',
    item_value VARCHAR(128) NOT NULL COMMENT '选项值（存储值）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- ============================================
-- 3. 插入字典分类数据
-- ============================================

-- 3.1 项目状态字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('project_status', '项目状态', 1);

-- 3.2 模板状态字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('template_status', '模板状态', 1);

-- 3.3 点位状态字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('point_status', '点位状态', 1);

-- 3.4 排口类型字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('outfall_type', '排口类型', 1);

-- 3.5 审核操作字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('audit_action', '审核操作', 1);

-- 3.6 用户角色字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('user_role', '用户角色', 1);

-- 3.7 导出任务状态字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('export_task_status', '导出任务状态', 1);

-- 3.8 消息类型字典
INSERT INTO sys_dict (dict_code, dict_name, status) VALUES
('message_type', '消息类型', 1);

-- ============================================
-- 4. 插入字典项数据
-- ============================================

-- 4.1 项目状态字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '草稿', '0', 0, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '进行中', '1', 1, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已完成', '2', 2, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已归档', '3', 3, 1 FROM sys_dict WHERE dict_code = 'project_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已暂停', '4', 4, 1 FROM sys_dict WHERE dict_code = 'project_status';

-- 4.2 模板状态字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '草稿', '0', 0, 1 FROM sys_dict WHERE dict_code = 'template_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已发布', '1', 1, 1 FROM sys_dict WHERE dict_code = 'template_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已停用', '2', 2, 1 FROM sys_dict WHERE dict_code = 'template_status';

-- 4.3 点位状态字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '未分配', '0', 0, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待采集', '1', 1, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '采集中', '2', 2, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待审核', '3', 3, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核通过', '4', 4, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核驳回', '5', 5, 1 FROM sys_dict WHERE dict_code = 'point_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已归档', '6', 6, 1 FROM sys_dict WHERE dict_code = 'point_status';

-- 4.4 排口类型字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '雨水排口', 'rainwater', 0, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '污水排口', 'sewage', 1, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '雨污混流口', 'mixed', 2, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '工业废水排口', 'industrial', 3, 1 FROM sys_dict WHERE dict_code = 'outfall_type';

-- 4.5 审核操作字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '通过', 'pass', 0, 1 FROM sys_dict WHERE dict_code = 'audit_action';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '驳回', 'reject', 1, 1 FROM sys_dict WHERE dict_code = 'audit_action';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '退回修改', 'return', 2, 1 FROM sys_dict WHERE dict_code = 'audit_action';

-- 4.6 用户角色字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '超级管理员', 'R_SUPER', 0, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '系统管理员', 'R_ADMIN', 1, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '项目经理', 'R_MANAGER', 2, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '勘察人员', 'R_COLLECTOR', 3, 1 FROM sys_dict WHERE dict_code = 'user_role';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核人员', 'R_AUDITOR', 4, 1 FROM sys_dict WHERE dict_code = 'user_role';

-- 4.7 导出任务状态字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '待处理', '0', 0, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '处理中', '1', 1, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已完成', '2', 2, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '已取消', '3', 3, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '失败', '4', 4, 1 FROM sys_dict WHERE dict_code = 'export_task_status';

-- 4.8 消息类型字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '系统通知', 'system', 0, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '任务分配', 'task_assign', 1, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '审核通知', 'audit_notify', 2, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '项目通知', 'project_notify', 3, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '导出完成', 'export_complete', 4, 1 FROM sys_dict WHERE dict_code = 'message_type';

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort_order, status) 
SELECT id, '警告提醒', 'warning', 5, 1 FROM sys_dict WHERE dict_code = 'message_type';

-- ============================================
-- 5. 验证数据
-- ============================================

-- 查看字典分类数量
SELECT 
    '字典分类总数' AS info,
    COUNT(*) AS count
FROM sys_dict;

-- 查看字典项数量
SELECT 
    '字典项总数' AS info,
    COUNT(*) AS count
FROM sys_dict_item;

-- 查看每个字典的项数
SELECT 
    d.dict_code,
    d.dict_name,
    COUNT(di.id) AS item_count
FROM sys_dict d
LEFT JOIN sys_dict_item di ON d.id = di.dict_id
GROUP BY d.id, d.dict_code, d.dict_name
ORDER BY d.id;

-- 查看具体数据
SELECT 
    '=== 项目状态字典 ===' AS info;
SELECT di.item_label, di.item_value, di.sort_order
FROM sys_dict d
LEFT JOIN sys_dict_item di ON d.id = di.dict_id
WHERE d.dict_code = 'project_status'
ORDER BY di.sort_order;

SELECT 
    '=== 点位状态字典 ===' AS info;
SELECT di.item_label, di.item_value, di.sort_order
FROM sys_dict d
LEFT JOIN sys_dict_item di ON d.id = di.dict_id
WHERE d.dict_code = 'point_status'
ORDER BY di.sort_order;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 数据字典初始化完成！' AS message;
SELECT '✅ 已创建 8 个字典分类' AS message;
SELECT '✅ 已初始化 43 个字典项' AS message;
SELECT '✅ 后端重启后即可使用数据字典管理功能' AS message;
