-- ============================================
-- 项目详情接口问题诊断脚本
-- 说明：检查项目 ID 303813117662986240 是否存在
-- ============================================

USE survey_db;

-- =============================================
-- 第1步：检查特定项目是否存在
-- =============================================

SELECT '========== 检查项目 ID: 303813117662986240 ==========' as step;

SELECT 
    id,
    project_name,
    project_code,
    status,
    create_time,
    update_time
FROM project
WHERE id = 303813117662986240;

-- =============================================
-- 第2步：如果上面没有结果，检查所有项目
-- =============================================

SELECT '========== 查看所有项目 ==========' as step;

SELECT 
    id,
    project_name,
    project_code,
    status,
    create_time
FROM project
ORDER BY id DESC
LIMIT 20;

-- =============================================
-- 第3步：检查项目总数
-- =============================================

SELECT '========== 项目统计 ==========' as step;

SELECT 
    COUNT(*) as total_projects,
    MIN(id) as min_id,
    MAX(id) as max_id
FROM project;

-- =============================================
-- 第4步：检查是否有类似的项目（可能是 ID 错误）
-- =============================================

SELECT '========== 查找相似的项目 ==========' as step;

-- 如果 ID 太长，可能是字符串类型的问题
SELECT 
    id,
    project_name,
    LENGTH(CAST(id AS CHAR)) as id_length
FROM project
ORDER BY id DESC
LIMIT 10;

-- =============================================
-- 第5步：创建测试项目（如果需要）
-- =============================================

SELECT '========== 创建测试项目（可选） ==========' as step;

-- 取消下面的注释以创建测试项目
/*
INSERT INTO project (
    project_name,
    project_code,
    description,
    status,
    template_count,
    point_count,
    completed_count,
    pending_audit_count,
    manager_id,
    create_time,
    update_time
) VALUES (
    '测试项目',
    'TEST-2024-001',
    '用于测试的项目',
    0,
    0,
    0,
    0,
    0,
    1,
    NOW(),
    NOW()
);

-- 查看新创建的项目
SELECT * FROM project WHERE project_code = 'TEST-2024-001';
*/

-- =============================================
-- 完成报告
-- =============================================

SELECT '========================================' as message;
SELECT '✅ 诊断完成！' as message;
SELECT '========================================' as message;
SELECT '请查看上面的检查结果：' as next_step;
SELECT '1. 如果项目不存在，需要创建项目或检查 ID 是否正确' as step1;
SELECT '2. 如果项目存在但接口返回空，检查后端日志' as step2;
SELECT '3. 如果 ID 类型不匹配，检查前端传递的 ID 格式' as step3;
