#!/bin/bash
# 部署前质量检查脚本
# 用于验证前后端接口兼容性和基本功能

set -e

echo "=========================================="
echo "       部署前质量检查"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

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

# 可配置的后端端口
BACKEND_PORT=${BACKEND_PORT:-8081}
BACKEND_BASE="http://localhost:${BACKEND_PORT}"

# 1. 检查后端服务是否运行
echo ""
echo "1. 检查后端服务状态..."
if curl -sf "${BACKEND_BASE}/api/auth/captcha" > /dev/null 2>&1; then
    pass "后端服务正常运行 (端口: ${BACKEND_PORT})"
else
    fail "后端服务未运行或无法访问 (${BACKEND_BASE})"
    echo "   请先启动后端服务: cd admin-backend && mvn spring-boot:run 或 ./deploy.sh"
fi

# 2. 检查关键接口
echo ""
echo "2. 检查关键接口..."

# 验证码接口
CAPTCHA_RESP=$(curl -s "${BACKEND_BASE}/api/auth/captcha")
if echo "$CAPTCHA_RESP" | jq -e '.code == 200' > /dev/null 2>&1; then
    pass "验证码接口正常"
else
    fail "验证码接口异常: $CAPTCHA_RESP"
fi

# 健康检查接口
HEALTH_RESP=$(curl -s "${BACKEND_BASE}/api/v1/health")
if echo "$HEALTH_RESP" | grep -q "UP"; then
    pass "健康检查接口正常"
else
    warn "健康检查接口异常: $HEALTH_RESP"
fi

# 3. 检查前端类型定义与后端是否匹配
echo ""
echo "3. 检查前端类型定义..."
FRONTEND_TYPES="admin-web-soybean/src/typings/api.d.ts"

if [ ! -f "$FRONTEND_TYPES" ]; then
    warn "前端类型定义文件不存在 ($FRONTEND_TYPES)，跳过检查"
else
    if grep -q "accessToken" "$FRONTEND_TYPES" && \
       grep -q "refreshToken" "$FRONTEND_TYPES" && \
       grep -q "userId" "$FRONTEND_TYPES"; then
        pass "前端 LoginToken 类型定义完整"
    else
        fail "前端 LoginToken 类型定义不完整"
    fi
fi

# 4. 检查前端 API 路径配置
echo ""
echo "4. 检查前端 API 路径配置..."
AUTH_API_FILE="admin-web-soybean/src/service/api/auth.ts"

if [ ! -f "$AUTH_API_FILE" ]; then
    warn "前端登录 API 文件不存在 ($AUTH_API_FILE)，跳过检查"
else
    if grep -q "fetchLogin" "$AUTH_API_FILE"; then
        pass "前端登录 API 已定义"
    else
        fail "前端登录 API 未定义"
    fi
fi

# 5. 检查数据库连接
echo ""
echo "5. 检查数据库连接..."

# 通过后端健康检查接口验证
if curl -sf "${BACKEND_BASE}/api/v1/health" > /dev/null 2>&1; then
    pass "后端可达，数据库连接状态请查看健康检查响应"
else
    warn "无法检查数据库连接（后端服务未运行）"
fi

# 6. 检查 Docker 配置
echo ""
echo "6. 检查 Docker 配置..."

if [ -f "docker-compose.yml" ]; then
    pass "docker-compose.yml 存在"

    # 检查必要服务定义
    if grep -q "admin-backend" docker-compose.yml; then
        pass "Docker admin-backend 服务定义完整"
    else
        fail "Docker admin-backend 服务未定义"
    fi

    if grep -q "mysql" docker-compose.yml; then
        pass "Docker mysql 服务已定义"
    else
        fail "Docker mysql 服务未定义"
    fi

    if grep -q "redis" docker-compose.yml; then
        pass "Docker redis 服务已定义"
    else
        fail "Docker redis 服务未定义"
    fi
else
    fail "docker-compose.yml 不存在"
fi

# 7. 检查 Dockerfile
echo ""
echo "7. 检查 Dockerfile..."
if [ -f "admin-backend/Dockerfile" ]; then
    pass "后端 Dockerfile 存在"
else
    fail "后端 Dockerfile 不存在"
fi

if [ -f ".dockerignore" ]; then
    pass ".dockerignore 存在"
else
    warn ".dockerignore 不存在，建议创建以减小构建上下文"
fi

# 8. 检查环境配置
echo ""
echo "8. 检查环境配置..."
if [ -f ".env" ]; then
    pass "Docker 环境配置文件 (.env) 存在"
else
    if [ -f ".env.example" ]; then
        warn ".env 不存在，请先执行: cp .env.example .env"
    else
        fail ".env.example 文件也不存在"
    fi
fi

# 9. 运行后端单元测试（可选）
echo ""
echo "9. 检查后端构建状态..."
if [ -f "admin-backend/pom.xml" ]; then
    if [ -f "admin-backend/target/*.jar" ]; then
        # 使用通配符检查
        JAR_COUNT=$(ls admin-backend/target/*.jar 2>/dev/null | wc -l)
        if [ "$JAR_COUNT" -gt 0 ]; then
            pass "后端已编译（找到 $JAR_COUNT 个 jar 包）"
        else
            warn "未找到编译产物，需要先编译或使用Docker构建"
        fi
    else
        warn "未找到编译产物，需要先编译或使用Docker构建"
    fi
else
    fail "admin-backend/pom.xml 不存在"
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
    echo "${RED}存在检查失败项（$FAIL_COUNT个），请修复后再部署！${NC}"
    exit 1
else
    echo "${GREEN}所有检查通过，可以继续部署！${NC}"
    exit 0
fi
