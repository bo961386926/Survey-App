#!/bin/bash

# ========================================
# 快速部署脚本 - 优化版
# 特点：利用Maven本地缓存，加速构建
# ========================================

set -e

echo "======================================"
echo "  Survey-App 快速部署（优化版）"
echo "======================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 方案1: 本地编译 + Docker打包（最快）
echo -e "${YELLOW}选择构建方式：${NC}"
echo "1) 本地编译 + Docker打包（推荐，最快 ~30秒）"
echo "2) Docker完整构建（较慢 ~3-5分钟）"
echo ""
read -p "请选择 [1/2] (默认1): " choice

if [ "$choice" = "2" ]; then
    # 方案2: 完整Docker构建
    echo ""
    echo -e "${YELLOW}正在使用Docker完整构建...${NC}"
    docker compose build admin-backend
    docker compose up -d
else
    # 方案1: 本地编译（利用Maven缓存）
    echo ""
    echo -e "${YELLOW}[1/4] 本地编译Java项目...${NC}"
    cd admin-backend
    
    # 检查Maven
    if command -v mvn &> /dev/null; then
        MAVEN_CMD="mvn"
    elif [ -f "apache-maven-3.9.6/bin/mvn" ]; then
        MAVEN_CMD="./apache-maven-3.9.6/bin/mvn"
    else
        echo -e "${YELLOW}未找到Maven，将使用Docker构建...${NC}"
        cd ..
        docker compose build admin-backend
        docker compose up -d
        exit 0
    fi
    
    # 本地编译（利用~/.m2缓存）
    $MAVEN_CMD clean package -DskipTests -q
    echo -e "${GREEN}✓ 编译完成${NC}"
    cd ..
    
    echo ""
    echo -e "${YELLOW}[2/4] 停止现有服务...${NC}"
    docker compose down -q
    
    echo ""
    echo -e "${YELLOW}[3/4] 快速构建Docker镜像（仅打包）...${NC}"
    # 使用优化版Dockerfile
    docker build -f Dockerfile.fast -t survey-app-admin-backend ./admin-backend
    
    echo ""
    echo -e "${YELLOW}[4/4] 启动服务...${NC}"
    docker compose up -d
    
    echo ""
    echo -e "${GREEN}✓ 部署完成！${NC}"
fi

echo ""
sleep 10

echo "======================================"
echo "  服务状态"
echo "======================================"
docker compose ps

echo ""
echo "======================================"
echo "  访问地址"
echo "======================================"
echo -e "${GREEN}后端API:${NC} http://localhost:8081"
echo -e "${GREEN}API文档:${NC} http://localhost:8081/doc.html"
echo -e "${GREEN}健康检查:${NC} http://localhost:8081/api/v1/health"
echo ""
