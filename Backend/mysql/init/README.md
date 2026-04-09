# MySQL 初始化脚本

## 文件说明

| 文件 | 说明 |
|------|------|
| `0-init.sql` | **完整初始化脚本** - 包含所有表的建表语句和基础数据（角色、管理员、菜单） |

## 初始化顺序

Docker Compose 启动 MySQL 容器时，会自动按字母顺序执行 `/docker-entrypoint-initdb.d/` 目录下的 `.sql` 文件。

## 使用方法

### Docker Compose 自动初始化
```bash
docker compose -f Backend/docker-compose.yml up -d mysql
```
容器首次启动时会自动执行 `0-init.sql`

### 手动初始化
```powershell
# 使用项目根目录的初始化脚本
.\init-database.ps1
```

## 包含内容

### 表结构 (20+ 表)
- 商品模块: goods_category, goods, sku, sn_code, sn_code_log, goods_stats, spec_name, spec_value
- 客户模块: member_level_config, customer, address, balance_record, points_record, user_behavior_log, page_stats, user_retained_stats
- 购物车: cart
- 订单模块: `order`, order_item, refund_order
- 系统模块: department, admin_user, role, menu, admin_log

### 基础数据
- 预设角色: 超级管理员、运营主管、录入员
- 预设管理员: admin / 123456
- 预设菜单: 仪表盘、商品管理、SN码管理、订单管理、客户管理、数据统计、系统管理
