@echo off
chcp 65001 > nul
echo ============================================
echo 数据字典 V2 - 诊断和启动脚本
echo ============================================
echo.

echo [步骤 1] 检查后端服务状态...
echo.
echo 请检查后端服务是否在运行：
echo - 如果正在运行，请先停止（Ctrl+C）
echo - 如果未运行，直接继续下一步
echo.
pause

echo.
echo [步骤 2] 进入后端目录...
cd /d "%~dp0admin-backend"

echo.
echo [步骤 3] 清理旧的编译文件...
call mvn clean
if errorlevel 1 (
    echo.
    echo ❌ Maven clean 失败！
    pause
    exit /b 1
)

echo.
echo [步骤 4] 编译项目...
call mvn package -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ Maven package 失败！请检查编译错误。
    pause
    exit /b 1
)

echo.
echo ✅ 编译成功！
echo.

echo [步骤 5] 启动后端服务...
echo.
echo 正在启动 Spring Boot 应用...
echo 请等待启动完成（看到 "Started SurveyApplication" 字样）
echo.
echo ============================================
echo 提示：按 Ctrl+C 可以停止服务
echo ============================================
echo.

call mvn spring-boot:run
