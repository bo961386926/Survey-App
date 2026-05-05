#!/bin/bash
# 部署前检查脚本
# 用于验证前后端接口兼容性和基本功能

set -e

echo "=========================================="
echo "       部署前质量检查"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果
PASS_COUNT=0
FAIL_COUNT=0

pass() {
    echo "${GREEN}[PASS]${NC} $1"
    PASS_COUNT=$((PASS_COUNT + 1))
}

fail() {
    echo "${RED}[FAIL]${NC} $1"
    FAIL_COUNT=$((FAIL_COUNT + 1))
}

warn() {
    echo "${YELLOW}[WARN]${NC} $1"
}

# 1. 检查后端服务是否运行
echo ""
echo "1. 检查后端服务状态..."
if curl -sf http://localhost:8080/api/auth/captcha > /dev/null 2>&1; then
    pass "后端服务正常运行"
else
    fail "后端服务未运行或无法访问"
    echo "   请先启动后端服务: cd admin-backend && mvn spring-boot:run"
fi

# 2. 检查关键接口
echo ""
echo "2. 检查关键接口..."

# 验证码接口
CAPTCHA_RESP=$(curl -s http://localhost:8080/api/auth/captcha)
if echo "$CAPTCHA_RESP" | jq -e '.code == 200' > /dev/null 2>&1; then
    pass "验证码接口正常"
else
    fail "验证码接口异常: $CAPTCHA_RESP"
fi

# 3. 检查前端类型定义与后端是否匹配
echo ""
echo "3. 检查前端类型定义..."

# 检查 LoginToken 类型是否包含必要字段
if grep -q "accessToken" admin-web-soybean/src/typings/api.d.ts && \
   grep -q "refreshToken" admin-web-soybean/src/typings/api.d.ts && \
   grep -q "userId" admin-web-soybean/src/typings/api.d.ts; then
    pass "前端 LoginToken 类型定义完整"
else
    fail "前端 LoginToken 类型定义不完整"
fi

# 4. 检查前端 API 路径配置
echo ""
echo "4. 检查前端 API 路径配置..."

if grep -q "fetchLogin" admin-web-soybean/src/service/api/auth.ts; then
    pass "前端登录 API 已定义"
else
    fail "前端登录 API 未定义"
fi

# 5. 检查数据库连接
echo ""
echo "5. 检查数据库连接..."

# 通过后端健康检查接口验证
if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    pass "数据库连接正常"
else
    warn "无法检查数据库连接（可能未启用 actuator）"
fi

# 6. 检查 Docker 配置
echo ""
echo "6. 检查 Docker 配置..."

if [ -f "docker-compose.yml" ]; then
    pass "docker-compose.yml 存在"
    
    # 检查必要服务定义
    if grep -q "admin-backend" docker-compose.yml && grep -q "admin-web" docker-compose.yml; then
        pass "Docker 服务定义完整"
    else
        fail "Docker 服务定义不完整"
    fi
else
    fail "docker-compose.yml 不存在"
fi

# 7. 检查环境配置
echo ""
echo "7. 检查环境配置..."

if [ -f "admin-web-soybean/.env" ]; then
    pass "前端环境配置文件存在"
    
    # 检查关键配置
    if grep -q "VITE_SERVICE_BASE_URL" admin-web-soybean/.env; then
        pass "后端服务地址已配置"
    else
        fail "后端服务地址未配置"
    fi
else
    fail "前端环境配置文件不存在"
fi

# 8. 运行后端单元测试
echo ""
echo "8. 运行后端单元测试..."
if [ -d "admin-backend" ]; then
    cd admin-backend
    if mvn test -q 2>&1 | tail -1 | grep -q "SUCCESS"; then
        pass "后端单元测试通过"
    else
        fail "后端单元测试失败"
    fi
    cd ..
else
    fail "admin-backend 目录不存在"
fi

# 输出结果
echo ""
echo "=========================================="
echo "       检查结果汇总"
echo "=========================================="
echo "${GREEN}通过: $PASS_COUNT${NC}"
echo "${RED}失败: $FAIL_COUNT${NC}"
echo ""

if [ $FAIL_COUNT -gt 0 ]; then
    echo "${RED}存在检查失败项，请修复后再部署！${NC}"
    exit 1
else
    echo "${GREEN}所有检查通过，可以继续部署！${NC}"
    exit 0
fi
