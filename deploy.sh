#!/bin/bash

# ========================================
# Survey-App Docker 部署脚本
# 功能：一键部署应用到Docker环境
# ========================================

set -e

echo "=========================================="
echo "  Survey-App Docker 部署脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
    exit 1
fi

# 检查Docker Compose (V2)
if ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装，请先安装Docker Compose${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker环境检查通过${NC}"
echo ""

# 检查.env文件
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        echo -e "${YELLOW}⚠ 未找到.env文件，从.env.example复制...${NC}"
        cp .env.example .env
        echo -e "${YELLOW}请编辑.env文件配置环境变量（建议修改默认密码）${NC}"
        echo ""
    else
        echo -e "${RED}错误: 缺少 .env.example 文件，无法生成配置文件${NC}"
        exit 1
    fi
fi

# 让脚本自身的健康检查也使用 .env 中的端口和密码配置。
set -a
source .env
set +a

# 询问是否清理旧数据
echo -e "${YELLOW}是否清理旧的 Docker 数据卷？【注意：会删除所有数据库和缓存数据】${NC}"
read -p "是否清理？(y/N): " clean_volumes
echo ""

# 停止并清理旧容器
echo -e "${CYAN}[1/4] 停止旧容器...${NC}"
if [ "$clean_volumes" = "y" ] || [ "$clean_volumes" = "Y" ]; then
    docker compose down -v
    echo -e "${GREEN}✓ 旧容器和数据卷已清理${NC}"
else
    docker compose down
    echo -e "${GREEN}✓ 旧容器已停止${NC}"
fi
echo ""

# 构建并启动服务
echo -e "${CYAN}[2/4] 开始构建和启动服务...${NC}"
docker compose up -d --build
echo -e "${GREEN}✓ 服务已启动${NC}"
echo ""

# 等待服务就绪
echo -e "${CYAN}[3/4] 等待服务就绪...${NC}"
echo "等待MySQL和Redis健康检查通过..."
sleep 15

# 健康检查
echo ""
echo -e "${CYAN}[4/4] 执行健康检查...${NC}"
ALL_HEALTHY=true

# 检查所有容器状态
echo ""
echo "容器运行状态:"
docker compose ps

echo ""

# 检查MySQL
echo -n "  MySQL: "
if docker compose exec -T mysql mysqladmin ping -h localhost -u root -p"${DB_ROOT_PASSWORD:-root}" &> /dev/null; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${RED}✗ 启动失败${NC}"
    ALL_HEALTHY=false
fi

# 检查Redis
echo -n "  Redis: "
REDIS_PASS=${REDIS_PASSWORD:-redis_password}
if docker compose exec -T redis redis-cli -a "$REDIS_PASS" ping 2>/dev/null | grep -q "PONG"; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${RED}✗ 启动失败${NC}"
    ALL_HEALTHY=false
fi

# 检查后端服务（通过宿主端口 8081）
echo -n "  Backend: "
sleep 5  # 给后端更多启动时间
BACKEND_PORT=${BACKEND_PORT:-8081}
if curl -s "http://localhost:${BACKEND_PORT}/api/v1/health" | grep -q "UP"; then
    echo -e "${GREEN}✓ 运行正常${NC}"
else
    echo -e "${YELLOW}⚠ 可能还在启动中，请稍后检查${NC}"
    echo -e "${YELLOW}  查看日志: docker compose logs -f admin-backend${NC}"
    ALL_HEALTHY=false
fi

echo ""
echo "=========================================="
if [ "$ALL_HEALTHY" = true ]; then
    echo -e "${GREEN}  所有服务部署完成！${NC}"
else
    echo -e "${YELLOW}  部署完成（部分服务可能仍在上电）${NC}"
fi
echo "=========================================="
echo ""
echo "📱 访问地址："
echo "   - PC管理后台:    http://localhost:${ADMIN_WEB_PORT:-9527}"
echo "   - 移动端H5:      http://localhost:${MOBILE_WEB_PORT:-3000}"
echo "   - 后端API:       http://localhost:${BACKEND_PORT:-8081}"
echo "   - 健康检查:      http://localhost:${BACKEND_PORT:-8081}/api/v1/health"
echo "   - API文档:       http://localhost:${BACKEND_PORT:-8081}/doc.html"
echo ""
echo "📋 常用命令："
echo "   - 查看日志:      docker compose logs -f admin-backend"
echo "   - 查看全部日志:  docker compose logs -f"
echo "   - 停止服务:      docker compose down"
echo "   - 重启服务:      docker compose restart"
echo ""
echo "📝 数据库连接信息："
echo "   - Host:      localhost:${MYSQL_PORT:-3306}"
echo "   - Database:  ${DB_NAME:-survey_db}"
echo "   - Username:  ${DB_USERNAME:-survey_user}"
echo "   - Password:  ${DB_PASSWORD:-survey_password}"
echo ""
