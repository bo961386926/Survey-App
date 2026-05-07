@echo off
REM 操作日志单元测试执行脚本 (Windows版本)
REM 使用方法: run-operation-log-tests.bat

echo =============================================
echo   操作日志单元测试执行脚本
echo =============================================
echo.

REM 进入项目目录
cd /d "%~dp0"

echo  项目目录: %CD%
echo.

REM 1. 清理并编译
echo [1/5] 清理并编译项目...
call mvn clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [失败] 编译失败！
    pause
    exit /b 1
)
echo [成功] 编译成功！
echo.

REM 2. 运行Service层测试
echo [2/5] 运行Service层单元测试...
call mvn test -Dtest=OperationLogServiceTest
if %ERRORLEVEL% NEQ 0 (
    echo [失败] Service层测试失败！
    pause
    exit /b 1
)
echo.

REM 3. 运行AOP层测试
echo [3/5] 运行AOP切面层单元测试...
call mvn test -Dtest=OperationLogAspectTest
if %ERRORLEVEL% NEQ 0 (
    echo [失败] AOP层测试失败！
    pause
    exit /b 1
)
echo.

REM 4. 运行集成测试
echo [4/5] 运行Controller集成测试...
call mvn test -Dtest=OperationLogIntegrationTest
if %ERRORLEVEL% NEQ 0 (
    echo [失败] 集成测试失败！
    pause
    exit /b 1
)
echo.

REM 5. 运行所有测试
echo [5/5] 运行所有操作日志相关测试...
call mvn test -Dtest=OperationLog*Test
if %ERRORLEVEL% NEQ 0 (
    echo [失败] 全部测试失败！
    pause
    exit /b 1
)
echo.

echo =============================================
echo   [成功] 所有测试通过！
echo =============================================
echo.
echo  测试报告已生成：
echo    - target\site\jacoco\index.html
echo.
echo  详细测试报告：
echo    - OPERATION_LOG_UNIT_TEST_REPORT.md
echo.

pause
