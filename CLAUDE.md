# CLAUDE.md

## 1. 主要目标

开发销售管理系统（小程序 + Web 端），实现：
- 商品管理与销售
- 客户资料管理
- 销售人员管理
- 销售数据统计

## 2. 目录结构

```
SaleManagerSys
├── Backed/                 # 后端项目 (SpringBoot)
├── Fronted/                # 前端 Web 项目 (Vue3)
├── Uniapp/                 # 小程序项目 (uni-app)
├── docs/                   # 项目文档
├── sql/                    # 数据库脚本
└── .claude/
    └── rules/              # 编码规范
```

## 3. 编码规范

遵循 `.claude/rules/` 下的规范：
- `coding-style.md` - 代码组织、命名、方法长度
- `frontend-style.md` - 前端 UI 设计规范
- `security.md` - 安全规则
- `testing.md` - 测试规范

## 4. 包结构

```
com.salemanager.modules.{module}/
├── controller/     # Controller
├── service/        # Service 接口
├── impl/           # Service 实现
├── mapper/         # Mapper 接口
├── model/          # 实体类
├── dto/            # 数据传输对象
├── vo/             # 视图对象
├── param/          # 请求参数
└── enums/          # 枚举类
```

## 5. 模块划分

| 模块 | 包名 | 说明 |
|------|------|------|
| 用户管理 | `modules.ums` | 用户、角色、权限 |
| 商品管理 | `modules.product` | 商品类别、商品信息 |
| 客户管理 | `modules.customer` | 客户资料 |
| 销售管理 | `modules.sale` | 销售订单、明细 |

## 6. 研发过程归档

- 【2026-03-27】项目初始化，创建基础目录结构
