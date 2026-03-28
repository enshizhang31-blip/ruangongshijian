# SaleManager 智能助手指令

## 快速参考

**项目**: 销售管理系统 (Sales Management System) - Vue3 + SpringBoot + UniApp
**架构**: 三端架构 - Fronted (Web管理端) + Uniapp (用户小程序) + Backend (后端API)
**主文档**: [CLAUDE.md](../CLAUDE.md)
**编码规范**: [.claude/rules/](../.claude/rules/) (coding-style.md, frontend-style.md, security.md, testing.md)

---

## 核心规范

### 1. 代码组织

- **前端 (Vue3)**: 使用 Composition API + `<script setup lang="ts">`
- **后端 (Java)**: 包结构 `com.salemanager.modules.{module}/`
- **文件限制**: Vue ≤300行, Java类 ≤500行, Controller ≤200行, Service ≤300行

### 2. Git 分支策略

```
main → Fronted/Backend/Uniapp → {module}-{name}
```

详见 [CLAUDE.md](../CLAUDE.md) 第3节

### 3. 前端技术栈

- **UI框架**: Arco Design Web Vue + Tailwind CSS
- **状态管理**: Pinia
- **路由**: Vue Router + 懒加载
- **构建**: Vite

### 4. 模块结构

| 模块                 | 说明                     |
| -------------------- | ------------------------ |
| `modules.ums`        | 员工、角色、权限         |
| `modules.product`    | 商品、分类               |
| `modules.sn`         | SN码管理(录入/查询/记录) |
| `modules.customer`   | 客户、会员               |
| `modules.sale`       | 订单、明细               |
| `modules.statistics` | 统计报表                 |

---

## 重要文件

| 文件                                                                  | 用途                     |
| --------------------------------------------------------------------- | ------------------------ |
| [CLAUDE.md](../CLAUDE.md)                                             | 项目主文档               |
| [.claude/rules/coding-style.md](../.claude/rules/coding-style.md)     | 代码组织、命名、方法长度 |
| [.claude/rules/frontend-style.md](../.claude/rules/frontend-style.md) | UI设计、颜色、组件       |
| [.claude/rules/security.md](../.claude/rules/security.md)             | 禁止硬编码、输入校验     |
| [.claude/rules/testing.md](../.claude/rules/testing.md)               | TDD、覆盖率≥80%          |
| [doc/研发日志.md](../doc/研发日志.md)                                 | 开发历史                 |
| [2026-03-27-需求详细设计.md](../doc/2026-03-27-需求详细设计.md)       | 需求详细设计             |

---

## 开发命令

### Fronted (Web管理端)

```sh
cd Fronted
npm install
npm run dev      # 开发模式
npm run build    # 生产构建
```

### UniApp (小程序)

```sh
cd Uniapp
npm install
npm run dev:mp-weixin   # 微信小程序
```

---

## 常用模式

### 前端 API 调用

```typescript
// Fronted/src/api/request.ts - Axios 封装
import request from "@/api/request";
const res = await request.post("/xxx", data);
```

### 前端组件结构

```vue
<script setup lang="ts">
// Composables、类型、导入
</script>
<template>
  <!-- Arco + Tailwind 混合使用 -->
</template>
```

### 动态路由加载

```typescript
// router/index.ts
component: () => import("@/views/xxx/XxxView.vue");
```

---

## 权限标识

| 权限标识         | 含义     |
| ---------------- | -------- |
| `product:view`   | 查看商品 |
| `product:add`    | 新增商品 |
| `product:edit`   | 编辑商品 |
| `product:delete` | 删除商品 |
| `order:view`     | 查看订单 |

---

## 注意事项

### 推荐

- 使用 `<script setup lang="ts">` 编写 Vue 组件
- 遵守文件大小限制 (Vue ≤300行)
- 使用 Arco Design 组件 + Tailwind 布局
- 返回 `Result<T>` 而非 null
- 使用有意义的变量名

### 禁止

- 硬编码密钥 (使用环境变量)
- 跳过输入校验
- 返回 null (使用 Result 包装)
- 使用 `temp/tmp` 等无意义变量名
- 编写 100+ 行的大方法
