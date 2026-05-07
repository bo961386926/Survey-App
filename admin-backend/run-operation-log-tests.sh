#!/bin/bash

# 操作日志单元测试执行脚本
# 使用方法: ./run-operation-log-tests.sh

echo "============================================="
echo "  操作日志单元测试执行脚本"
echo "============================================="
echo ""

# 进入项目目录
cd "$(dirname "$0")"

echo " 项目目录: $(pwd)"
echo ""

# 1. 清理并编译
echo "🔨 步骤1: 清理并编译项目..."
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败！"
    exit 1
fi
echo "✅ 编译成功！"
echo ""

# 2. 运行Service层测试
echo "🧪 步骤2: 运行Service层单元测试..."
mvn test -Dtest=OperationLogServiceTest
if [ $? -ne 0 ]; then
    echo "❌ Service层测试失败！"
    exit 1
fi
echo ""

# 3. 运行AOP层测试
echo " 步骤3: 运行AOP切面层单元测试..."
mvn test -Dtest=OperationLogAspectTest
if [ $? -ne 0 ]; then
    echo "❌ AOP层测试失败！"
    exit 1
fi
echo ""

# 4. 运行集成测试
echo "🧪 步骤4: 运行Controller集成测试..."
mvn test -Dtest=OperationLogIntegrationTest
if [ $? -ne 0 ]; then
    echo "❌ 集成测试失败！"
    exit 1
fi
echo ""

# 5. 运行所有测试
echo "🧪 步骤5: 运行所有操作日志相关测试..."
mvn test -Dtest=OperationLog*Test
if [ $? -ne 0 ]; then
    echo "❌ 全部测试失败！"
    exit 1
fi
echo ""

echo "============================================="
echo "  ✅ 所有测试通过！"
echo "============================================="
echo ""
echo "📊 测试报告已生成："
echo "   - target/site/jacoco/index.html"
echo ""
echo "📝 详细测试报告："
echo "   - OPERATION_LOG_UNIT_TEST_REPORT.md"
echo ""
