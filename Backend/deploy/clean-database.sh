#!/bin/bash
set -e

MYSQL_CONTAINER="salemanager-mysql"
MONGO_CONTAINER="salemanager-mongo"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "===== 1. 清空 MySQL 数据 ====="
docker exec -i "$MYSQL_CONTAINER" mysql -usalemanager -p'Sm@MySQL2026User' sale_manager <<'SQL'
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sn_code_log;
TRUNCATE TABLE sn_code;
TRUNCATE TABLE sku;
TRUNCATE TABLE goods;
TRUNCATE TABLE spec_value;
TRUNCATE TABLE spec_name;
TRUNCATE TABLE goods_category;
SET FOREIGN_KEY_CHECKS = 1;
SQL
echo "MySQL 清空完成"

echo "===== 2. 清空 MongoDB 数据 ====="
docker exec -i "$MONGO_CONTAINER" mongosh "mongodb://salemanager:Sm@Mongo2026Admin@localhost:27017/?authSource=admin" --quiet <<'MONGO'
use sale_manager_i18n
db.translation_units.deleteMany({})
MONGO
echo "MongoDB 清空完成"

echo "===== 清空完成 ====="
echo "如需重新初始化，请执行: bash init-database.sh"
