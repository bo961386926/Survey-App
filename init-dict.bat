@echo off
chcp 65001 > nul
REM ============================================
REM 数据字典初始化脚本 (Windows版本)
REM ============================================

echo ==========================================
echo 开始初始化数据字典...
echo ==========================================

REM 检查MySQL连接
echo.
echo 1. 检查MySQL连接...
docker exec survey-mysql mysql -uroot -proot -e "SELECT 1;" > nul 2>&1

if errorlevel 1 (
    echo ❌ MySQL连接失败，请检查Docker容器是否运行
    pause
    exit /b 1
)

echo ✅ MySQL连接成功

REM 执行SQL脚本
echo.
echo 2. 执行数据字典初始化SQL...
type init-sql\init_dict_data.sql | docker exec -i survey-mysql mysql -uroot -proot survey_db

if errorlevel 1 (
    echo ❌ 数据字典初始化失败
    pause
    exit /b 1
)

echo ✅ 数据字典初始化成功

REM 验证结果
echo.
echo 3. 验证初始化结果...
docker exec survey-mysql mysql -uroot -proot survey_db -e "SELECT (SELECT COUNT(*) FROM sys_dict) AS dict_count, (SELECT COUNT(*) FROM sys_dict_item) AS item_count;"

echo.
echo ==========================================
echo ✅ 数据字典初始化完成！
echo ==========================================
echo.
echo 下一步操作：
echo 1. 重新编译后端: cd admin-backend ^&^& mvn clean package -DskipTests
echo 2. 重启后端服务: docker-compose restart admin-backend
echo 3. 配置前端路由（参考 DICT_MANAGEMENT_GUIDE.md）
echo 4. 访问系统管理 -^> 数据字典
echo.
pause
