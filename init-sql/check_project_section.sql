-- ============================================
-- project_section 表数据检查和清理脚本
-- 说明：检查标段数据准确性，发现并修复问题
-- ============================================

USE survey_db;

-- =============================================
-- 第1步：检查表结构和索引
-- =============================================

SELECT '========== 检查表结构 ==========' as step;
DESCRIBE project_section;

SELECT '========== 检查唯一性约束 ==========' as step;
SHOW INDEX FROM project_section;

-- =============================================
-- 第2步：分析数据问题
-- =============================================

SELECT '========== 检查重复数据 ==========' as step;

-- 检查是否有重复的 (project_id, section_code) 组合
SELECT 
    project_id,
    section_code,
    COUNT(*) as duplicate_count
FROM project_section
GROUP BY project_id, section_code
HAVING COUNT(*) > 1;

-- 检查是否有重复的 section_name（考虑乱码）
SELECT 
    project_id,
    section_name,
    section_code,
    COUNT(*) as count
FROM project_section
GROUP BY project_id, section_name, section_code
HAVING COUNT(*) > 1;

-- =============================================
-- 第3步：检查数据完整性
-- =============================================

SELECT '========== 检查关联项目是否存在 ==========' as step;

SELECT 
    ps.id,
    ps.project_id,
    ps.section_name,
    ps.section_code,
    CASE WHEN p.id IS NULL THEN '❌ 项目不存在' ELSE '✅ 项目存在' END as project_status
FROM project_section ps
LEFT JOIN project p ON ps.project_id = p.id
ORDER BY ps.id;

SELECT '========== 检查关联用户是否存在 ==========' as step;

SELECT 
    ps.id,
    ps.section_name,
    ps.manager_id,
    CASE WHEN u.id IS NULL THEN '❌ 用户不存在' ELSE '✅ 用户存在' END as manager_status
FROM project_section ps
LEFT JOIN sys_user u ON ps.manager_id = u.id
ORDER BY ps.id;

-- =============================================
-- 第4步：分析乱码问题
-- =============================================

SELECT '========== 检查 section_name 乱码 ==========' as step;

SELECT 
    id,
    project_id,
    section_name,
    section_code,
    LENGTH(section_name) as name_length,
    CHAR_LENGTH(section_name) as char_length,
    CASE 
        WHEN LENGTH(section_name) != CHAR_LENGTH(section_name) THEN '⚠️ 可能包含多字节字符'
        ELSE '✅ 正常'
    END as encoding_status
FROM project_section
ORDER BY id;

-- =============================================
-- 第5步：统计信息
-- =============================================

SELECT '========== 数据统计 ==========' as step;

SELECT 
    COUNT(*) as total_records,
    COUNT(DISTINCT project_id) as unique_projects,
    COUNT(DISTINCT section_code) as unique_codes,
    COUNT(DISTINCT manager_id) as unique_managers
FROM project_section;

SELECT '========== 按项目统计 ==========' as step;

SELECT 
    p.id as project_id,
    p.project_name,
    COUNT(ps.id) as section_count,
    GROUP_CONCAT(ps.section_code ORDER BY ps.id SEPARATOR ', ') as section_codes
FROM project p
LEFT JOIN project_section ps ON p.id = ps.project_id
GROUP BY p.id, p.project_name
ORDER BY p.id;

-- =============================================
-- 第6步：识别需要清理的数据
-- =============================================

SELECT '========== 识别重复记录（保留最早的） ==========' as step;

-- 找出所有重复的记录（除了每组中 id 最小的那条）
SELECT 
    ps1.id as duplicate_id,
    ps1.project_id,
    ps1.section_name,
    ps1.section_code,
    ps1.create_time,
    '应该删除' as action
FROM project_section ps1
INNER JOIN (
    SELECT project_id, section_code, MIN(id) as min_id
    FROM project_section
    GROUP BY project_id, section_code
    HAVING COUNT(*) > 1
) ps2 ON ps1.project_id = ps2.project_id 
    AND ps1.section_code = ps2.section_code
    AND ps1.id > ps2.min_id
ORDER BY ps1.id;

-- =============================================
-- 第7步：（可选）清理重复数据
-- =============================================

-- ⚠️ 警告：执行前先备份！
-- 取消下面的注释以执行清理

/*
-- 开始事务
START TRANSACTION;

-- 删除重复记录（保留每组中 id 最小的那条）
DELETE ps1 FROM project_section ps1
INNER JOIN (
    SELECT project_id, section_code, MIN(id) as min_id
    FROM project_section
    GROUP BY project_id, section_code
    HAVING COUNT(*) > 1
) ps2 ON ps1.project_id = ps2.project_id 
    AND ps1.section_code = ps2.section_code
    AND ps1.id > ps2.min_id;

-- 查看删除后的结果
SELECT '删除后的记录数' as info, COUNT(*) as count FROM project_section;

-- 确认无误后提交
COMMIT;

-- 如果有问题，可以回滚
-- ROLLBACK;
*/

-- =============================================
-- 第8步：添加唯一性约束（防止未来重复）
-- =============================================

SELECT '========== 添加唯一性约束 ==========' as step;

-- ⚠️ 警告：执行前确保没有重复数据！
-- 取消下面的注释以执行

/*
-- 添加唯一索引，防止 (project_id, section_code) 重复
ALTER TABLE project_section 
ADD UNIQUE INDEX uk_project_section (project_id, section_code);

-- 验证索引创建成功
SHOW INDEX FROM project_section;
*/

-- =============================================
-- 完成报告
-- =============================================

SELECT '========================================' as message;
SELECT '✅ 数据检查完成！' as message;
SELECT '========================================' as message;
SELECT '请查看上面的检查结果，确认是否需要清理数据' as next_step;
SELECT '⚠️ 发现的主要问题：' as warning;
SELECT '1. section_name 存在乱码（编码问题）' as issue1;
SELECT '2. 存在重复数据（相同的 project_id + section_code）' as issue2;
SELECT '3. 缺少唯一性约束' as issue3;
