#!/bin/bash
# ============================================
# 服务器端部署脚本
# 前提: 已手动上传 app.jar + application-prod.yml + sql/ + mongo-init.js
# 用法: bash deploy.sh
# ============================================
set -e

JAR="app.jar"
DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "===== 1. 停止旧进程 ====="
pkill -f "java -jar.*$JAR" || true
sleep 3
echo "已停止"

echo ""
echo "===== 2. 初始化数据库 ====="
bash "$DEPLOY_DIR/init-database.sh"

echo ""
echo "===== 3. 启动后端 ====="
cd "$DEPLOY_DIR"
mkdir -p logs
nohup java -jar $JAR --spring.profiles.active=prod > logs/app.log 2>&1 &
sleep 5

echo ""
echo "===== 4. 验证 ====="
STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health 2>/dev/null || echo "000")
if [ "$STATUS" = "200" ]; then
  echo "运行正常 (HTTP $STATUS)"
else
  echo "启动中或异常, 查看日志: tail -f $DEPLOY_DIR/logs/app.log"
fi

echo ""
echo "===== 完成 ====="
echo "后端地址: http://localhost:8080"
echo "停止服务: pkill -f java.*$JAR"