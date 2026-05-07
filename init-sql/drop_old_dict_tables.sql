-- ============================================
-- 删除旧版数据字典表
-- 说明：已迁移到 sys_dictionary 和 sys_dictionary_data
-- ============================================

USE survey_db;

-- 查看旧表是否存在
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME IN ('sys_dict', 'sys_dict_item')
ORDER BY TABLE_NAME;

-- 如果确认要删除，请取消下面的注释并执行
-- 警告：删除后数据不可恢复！

-- DROP TABLE IF EXISTS sys_dict_item;
-- DROP TABLE IF EXISTS sys_dict;

-- 验证删除结果
-- SELECT COUNT(*) AS remaining_tables 
-- FROM INFORMATION_SCHEMA.TABLES 
-- WHERE TABLE_SCHEMA = 'survey_db' 
--   AND TABLE_NAME IN ('sys_dict', 'sys_dict_item');

-- 验证新表存在
SELECT 
    '新版数据字典表' AS info,
    TABLE_NAME,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'survey_db' 
  AND TABLE_NAME IN ('sys_dictionary', 'sys_dictionary_data')
ORDER BY TABLE_NAME;
