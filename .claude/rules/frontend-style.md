# SaleManager Web 前端 UI 设计规范

本文档记录销售管理系统前端项目的 UI 风格，基于 **Arco Design Web Vue** + **Tailwind CSS** 混合使用。

---

## 一、配色系统

### Arco Design 主题色

| 用途 | 颜色名   | Hex       | Tailwind     |
| ---- | -------- | --------- | ------------ |
| 主色 | Arcoblue | `#0f62fe` | `blue-500`   |
| 成功 | Green    | `#00b42a` | `green-500`  |
| 警告 | Orange   | `#ff7d00` | `orange-500` |
| 危险 | Red      | `#f53f2c` | `red-500`    |
| 信息 | Purple   | `#722ed1` | `purple-500` |

### 功能色使用

```vue
<!-- Arco Tag 组件 -->
<Tag color="green">成功</Tag>
<Tag color="red">危险</Tag>

<!-- 或 Tailwind 工具类 -->
<span class="text-green-500">成功</span>
<span class="text-red-500">危险</span>
```

---

## 二、布局结构

### Desktop 布局 (≥768px)

```
┌─────────────────────────────────────┐
│ Header (h-14)                       │
├──────────┬──────────────────────────┤
│          │                          │
│ Sidebar  │     Main Content         │
│ (w-60)   │     (p-4 lg:p-6)        │
│          │                          │
│          │                          │
└──────────┴──────────────────────────┘
```

### Mobile 布局 (<768px)

```
┌─────────────────────┐
│                     │
│   Main Content      │
│   (p-4)             │
│                     │
│                     │
├─────────────────────┤
│   BottomNav (h-14)  │
└─────────────────────┘
```

### 响应式断点

| 断点 | 宽度     | Tailwind | 用途          |
| ---- | -------- | -------- | ------------- |
| xs   | < 576px  | `xs:`    | 超小屏手机    |
| sm   | ≥ 576px  | `sm:`    | 小屏平板      |
| md   | ≥ 768px  | `md:`    | 中屏/桌面分界 |
| lg   | ≥ 992px  | `lg:`    | 小屏桌面      |
| xl   | ≥ 1200px | `xl:`    | 大屏桌面      |

---

## 三、Arco Design 组件使用

### 布局组件

| 组件      | 用途         | 使用位置                 |
| --------- | ------------ | ------------------------ |
| `Card`    | 页面卡片容器 | 所有列表页搜索区、数据区 |
| `Row/Col` | 栅格布局     | Dashboard 统计卡片       |
| `Space`   | 元素间距     | 按钮组、表单项           |

### 数据组件

| 组件         | 用途     | 使用位置                   |
| ------------ | -------- | -------------------------- |
| `Table`      | 数据表格 | Product/Customer/Sale 列表 |
| `Pagination` | 分页     | 列表页底部                 |

### 表单组件

| 组件            | 用途     | 使用位置             |
| --------------- | -------- | -------------------- |
| `Form/FormItem` | 表单容器 | Login、新增/编辑弹窗 |
| `Input`         | 输入框   | 搜索、表单           |
| `Select`        | 下拉选择 | 表单筛选             |

### 反馈组件

| 组件         | 用途     | 使用位置   |
| ------------ | -------- | ---------- |
| `Button`     | 按钮     | 全局       |
| `Tag`        | 状态标签 | 表格行状态 |
| `Message`    | 消息提示 | 操作反馈   |
| `Popconfirm` | 确认弹窗 | 删除确认   |
| `Modal`      | 弹窗     | 详情/编辑  |

### 导航组件

| 组件            | 用途       | 使用位置        |
| --------------- | ---------- | --------------- |
| `Menu/MenuItem` | 侧边栏导航 | Sidebar         |
| `Avatar`        | 用户头像   | Header          |
| `Dropdown`      | 下拉菜单   | Header 用户菜单 |

### 数据展示

| 组件        | 用途     | 使用位置       |
| ----------- | -------- | -------------- |
| `Statistic` | 统计数字 | Dashboard 卡片 |
| `Skeleton`  | 骨架屏   | 加载占位       |

---

## 四、Tailwind CSS 使用规范

### 使用场景

- 响应式工具类：`hidden md:block`、`flex-col md:flex-row`
- 自定义间距：margin/padding 调整
- 文本颜色：`text-gray-500`、`text-blue-600`
- 背景颜色：`bg-white`、`bg-gray-50`
- 阴影：`shadow-sm hover:shadow-lg`
- 圆角：`rounded-lg`、`rounded-xl`

### Arco + Tailwind 优先级

```
Arco Design 组件属性 > Tailwind 工具类
```

```vue
<!-- ✅ 正确：Arco 属性优先 -->
<Button type="primary" class="ml-4">

<!-- ⚠️ 避免：重复样式 -->
<Button class="bg-blue-500 hover:bg-blue-600">
```

---

## 五、组件样式规范

### 页面标题区

```vue
<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
    <div>
        <h1 class="text-xl lg:text-2xl font-bold text-gray-800">商品管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理商品信息</p>
    </div>
    <Button type="primary">新增商品</Button>
</div>
```

### 搜索区域

```vue
<Card class="mb-4">
    <Space direction="horizontal" :size="12" wrap>
        <Input v-model="keyword" placeholder="搜索..." class="!w-64" />
        <Button type="primary" @click="handleSearch">搜索</Button>
        <Button @click="handleReset">重置</Button>
    </Space>
</Card>
```

### 数据表格

```vue
<Card>
    <Table :columns="columns" :data="list" :pagination="false" :scroll="{ x: 800 }">
        <template #actions="{ record }">
            <Space>
                <Button type="text" size="small">编辑</Button>
                <Popconfirm title="确定删除？" @ok="handleDelete(record.id)">
                    <Button type="text" status="danger" size="small">删除</Button>
                </Popconfirm>
            </Space>
        </template>
    </Table>
</Card>
```

### 统计卡片 (Dashboard)

```vue
<Row :gutter="[16, 16]">
    <Col :xs="24" :sm="12" :xl="6">
        <Card class="hover:shadow-lg transition-all cursor-pointer">
            <Statistic title="今日销售额" :value="1000" prefix="¥" />
        </Card>
    </Col>
</Row>
```

---

## 六、图标规范

使用 **Heroicons** (Outline 风格)

```vue
import { PlusIcon, PencilIcon } from '@heroicons/vue/24/outline'

<Button>
    <template #icon><PlusIcon class="w-4 h-4" /></template>
    新增
</Button>
```

### 常用图标

| 用途 | 图标               |
| ---- | ------------------ |
| 首页 | `ViewColumnsIcon`  |
| 商品 | `CubeIcon`         |
| 客户 | `UsersIcon`        |
| 订单 | `ShoppingCartIcon` |
| 统计 | `ChartBarIcon`     |
| 设置 | `Cog6ToothIcon`    |
| 新增 | `PlusIcon`         |
| 编辑 | `PencilIcon`       |
| 删除 | `TrashIcon`        |
| 查看 | `EyeIcon`          |

---

## 七、圆角规范

| 用途   | Arco          | Tailwind       |
| ------ | ------------- | -------------- |
| 按钮   | 默认          | `rounded-lg`   |
| 输入框 | 默认          | `rounded-lg`   |
| 卡片   | 默认          | `rounded-xl`   |
| 弹窗   | `rounded-2xl` | `rounded-2xl`  |
| 头像   | `circle`      | `rounded-full` |

---

## 八、开发注意事项

1. **Arco + Tailwind 混合**：Arco 负责复杂组件，Tailwind 负责布局和微调
2. **响应式优先**：移动端布局先行，使用 `flex-col`、`gap-4` 等
3. **Arco 栅格**：`Row/Col` 用于需要等分布置的区域
4. **Tailwind 响应式**：`xs:` `sm:` `md:` `lg:` `xl:` 分断点
5. **表格滚动**：`Table` 设置 `:scroll="{ x: 800 }"` 启用水平滚动
6. **卡片悬停**：`hover:shadow-lg transition-all`
7. **表单验证**：使用 Arco `Form` 组件的校验功能
