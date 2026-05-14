#!/bin/bash
set -e

MYSQL_CONTAINER="salemanager-mysql"
MONGO_CONTAINER="salemanager-mongo"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "===== 1. MySQL DDL ====="
docker exec -i "$MYSQL_CONTAINER" mysql -usalemanager -p'Sm@MySQL2026!User' sale_manager < "$SCRIPT_DIR/sql/schema.sql"
echo "OK"

echo "===== 2. MongoDB ====="
docker exec -i "$MONGO_CONTAINER" mongosh "mongodb://salemanager:Sm@Mongo2026!Admin@localhost:27017/?authSource=admin" --quiet < "$SCRIPT_DIR/mongo-init.js"
echo "OK"

echo "===== 完成 ====="