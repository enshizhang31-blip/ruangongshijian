# MySQL 数据库一键初始化脚本
# 使用方法: .\init-database.ps1

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "MySQL 数据库一键初始化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$MySQLContainer = "salemanager-mysql"
$DatabaseName = "sale_manager"
$InitFile = "mysql/init/0-init.sql"

# 1. 停止并删除 MySQL 容器
Write-Host "`n[1/4] 停止并删除 MySQL 容器..." -ForegroundColor Yellow
docker compose -f Backend/docker-compose.yml down mysql 2>$null
if ($LASTEXITCODE -ne 0) {
    docker stop $MySQLContainer 2>$null
    docker rm $MySQLContainer 2>$null
}

# 2. 删除旧数据卷
Write-Host "[2/4] 删除旧数据卷..." -ForegroundColor Yellow
$volumeName = "backend_mysql-data"
docker volume rm $volumeName 2>$null

# 3. 重新创建 MySQL 容器
Write-Host "[3/4] 创建新的 MySQL 容器..." -ForegroundColor Yellow
docker compose -f Backend/docker-compose.yml up -d mysql

# 4. 等待 MySQL 就绪
Write-Host "[4/4] 等待 MySQL 启动..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0
while ($waited -lt $maxWait) {
    $result = docker exec $MySQLContainer mysqladmin ping -uroot -proot123 2>$null
    if ($result -match "mysqld is alive") {
        Write-Host "MySQL 启动成功!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $waited += 2
    Write-Host "等待 MySQL 就绪... ($waited/$maxWait 秒)" -ForegroundColor Gray
}

if ($waited -ge $maxWait) {
    Write-Host "MySQL 启动超时!" -ForegroundColor Red
    exit 1
}

# 5. 执行初始化脚本
Write-Host "`n执行初始化脚本..." -ForegroundColor Yellow
docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 -e "DROP DATABASE IF EXISTS $DatabaseName; CREATE DATABASE $DatabaseName;" 2>$null
docker cp $InitFile "${MySQLContainer}:/tmp/init.sql"
docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 $DatabaseName -e "source /tmp/init.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "数据库初始化成功!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    # 显示初始化结果
    Write-Host "`n初始化的表:" -ForegroundColor Cyan
    docker exec $MySQLContainer mysql -uroot -proot123 --default-character-set=utf8mb4 $DatabaseName -e "SHOW TABLES;" 2>$null | Select-Object -Skip 1
    
    Write-Host "`n预设管理员账户:" -ForegroundColor Cyan
    Write-Host "用户名: admin" -ForegroundColor White
    Write-Host "密码: 123456" -ForegroundColor White
} else {
    Write-Host "`n数据库初始化失败!" -ForegroundColor Red
    exit 1
}
