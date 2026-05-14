#!/bin/bash
# ============================================
# 数据库初始化脚本 (MySQL + MongoDB)
# 用法: bash init-database.sh
# ============================================
set -e

# --- 从 .env 读取密码 (如果存在) ---
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

MYSQL_CONTAINER="${MYSQL_CONTAINER:-salemanager-mysql}"
MYSQL_USER="${MYSQL_USER:-salemanager}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-Sm@MySQL2026!User}"
MYSQL_DATABASE="${MYSQL_DATABASE:-sale_manager}"

MONGO_CONTAINER="${MONGO_CONTAINER:-salemanager-mongo}"
MONGO_USER="${MONGO_USER:-salemanager}"
MONGO_PASSWORD="${MONGO_PASSWORD:-Sm@Mongo2026!Admin}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "===== 1. MySQL 表结构 (DDL) ====="
docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < "$PROJECT_DIR/sql/schema.sql"
echo "OK"

echo ""
echo "===== 2. MySQL 种子数据 (DML) ====="
docker exec -i "$MYSQL_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < "$PROJECT_DIR/sql/seed-data.sql"
echo "OK"

echo ""
echo "===== 3. MongoDB 初始化 ====="
docker exec -i "$MONGO_CONTAINER" mongosh -u "$MONGO_USER" -p "$MONGO_PASSWORD" --authenticationDatabase admin --quiet < "$PROJECT_DIR/sql/mongo-init.js"
echo "OK"

echo ""
echo "===== 完成 ====="
echo "MySQL:  用户 $MYSQL_USER@$MYSQL_CONTAINER → $MYSQL_DATABASE"
echo "MongoDB: 用户 $MONGO_USER@$MONGO_CONTAINER → sale_manager_i18n"
echo ""
echo "验证:"
echo "  docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE -e 'SHOW TABLES;'"
echo "  docker exec -i $MONGO_CONTAINER mongosh -u $MONGO_USER -p $MONGO_PASSWORD --authenticationDatabase admin --quiet --eval 'db.getSiblingDB(\"sale_manager_i18n\").translation_units.getIndexes()'"
