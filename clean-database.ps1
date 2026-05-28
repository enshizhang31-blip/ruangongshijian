# 数据库快速清空脚本（只清数据，不重建容器）
# 使用方法: .\clean-database.ps1

$MySQLContainer = "salemanager-mysql"
$MongoContainer = "salemanager-mongo"

Write-Host "========================================"
Write-Host "Quick Database Cleanup Script"
Write-Host "========================================"

Write-Host ""
Write-Host "========== MySQL =========="
Write-Host "Truncating all business tables..."
$sql = @"
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE goods_stats;
TRUNCATE TABLE sn_code_log;
TRUNCATE TABLE sn_code;
TRUNCATE TABLE order_item;
TRUNCATE TABLE `order`;
TRUNCATE TABLE balance_record;
TRUNCATE TABLE points_record;
TRUNCATE TABLE address;
TRUNCATE TABLE customer;
TRUNCATE TABLE member_level_config;
TRUNCATE TABLE sku;
TRUNCATE TABLE goods;
TRUNCATE TABLE spec_value;
TRUNCATE TABLE spec_name;
TRUNCATE TABLE goods_category;
TRUNCATE TABLE department;
TRUNCATE TABLE admin_log;
SET FOREIGN_KEY_CHECKS = 1;
"@
$sql | docker exec -i $MySQLContainer mysql -usalemanager -p'Sm@MySQL2026User' sale_manager
Write-Host "MySQL data cleared!" -ForegroundColor Green

Write-Host ""
Write-Host "========== MongoDB =========="
Write-Host "Clearing translation data..."
$mongoCmd = 'db.getSiblingDB("sale_manager_i18n").translation_units.deleteMany({})'
docker exec -i $MongoContainer mongosh "mongodb://salemanager:Sm@Mongo2026Admin@localhost:27017/?authSource=admin" --quiet --eval $mongoCmd
Write-Host "MongoDB data cleared!" -ForegroundColor Green

Write-Host ""
Write-Host "========================================"
Write-Host "Data cleared. Containers still running."
Write-Host "To re-import seed data, run: .\init-database.ps1"
Write-Host "========================================"
