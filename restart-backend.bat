@echo off
chcp 65001 > nul
echo ============================================
echo 重新启动后端服务（包含数据字典功能）
echo ============================================
echo.

cd admin-backend

echo [1/3] 停止现有服务...
echo 请手动停止正在运行的后端服务（Ctrl+C）
echo.
pause

echo [2/3] 编译项目...
call mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo ❌ 编译失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo ✅ 编译成功！
echo.

echo [3/3] 启动后端服务...
echo.
call mvn spring-boot:run
