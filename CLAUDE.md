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

每个**大的推进步骤**都需要记录归档，存放在 `doc/` 目录下。

### 归档要求

| 要求 | 说明 |
|------|------|
| **多文件记录** | 每个大里程碑独立一个文件 |
| **命名规范** | `doc/YYYY-MM-DD-里程碑名称.md` |
| **内容要素** | 技术决策、目录结构变化、组件清单、UI规范、问题解决 |
| **CLAUDE.md 摘要** | 仅记录简短摘要，指向 doc/ 详细文档 |

### 归档内容模板

```markdown
# YYYY-MM-DD 里程碑名称

## 背景
为什么做这个决策/改动

## 技术决策
- 选型理由
- 替代方案对比

## 目录结构
当前最新的项目结构

## 组件/模块清单
新增或改动的组件列表

## UI 设计规范
颜色、断点、布局等

## 问题与解决
遇到的问题及解决方案

## 下一步
后续计划
```

### 归档清单

- 【2026-03-27】项目初始化，创建基础目录结构 → `doc/2026-03-27-项目初始化.md`
- 【2026-03-27】前端项目搭建 → `doc/2026-03-27-前端项目搭建.md`
- 【2026-03-27】组件封装 → `doc/2026-03-27-组件封装.md`
- 【2026-03-27】Arco Design + Tailwind 重构 → `doc/2026-03-27-ArcoDesign重构.md`
