# SaleManager Web 前端 UI 设计规范

本文档记录销售管理系统前端项目的 UI 风格，基于 Tailwind CSS。

---

## 一、配色系统

### 主题色

```css
--color-primary: #6366f1; /* 品牌紫 */
--color-primary-light: #818cf8; /* 浅紫 */
--color-primary-dark: #4f46e5; /* 深紫 */
```

### 功能色

```css
--color-success: #10b981; /* 绿色-成功 */
--color-warning: #f59e0b; /* 黄色-警告 */
--color-danger: #ef4444; /* 红色-危险 */
--color-info: #3b82f6; /* 蓝色-信息 */
```

### Tailwind CSS 使用方式

```html
<!-- 主题色 -->
<button class="bg-primary text-white hover:bg-primary-dark">按钮</button>

<!-- 功能色 -->
<span class="text-success">成功</span>
<span class="text-danger">危险</span>
```

---

## 二、布局结构

### 侧边栏

```html
<!-- 宽度: 260px 展开 / 72px 折叠 -->
<aside class="w-[260px]">...</aside>

<!-- 白色背景 + 阴影 -->
<div class="bg-white shadow-sm">...</div>

<!-- Logo区渐变 -->
<div class="bg-gradient-to-br from-primary to-primary-dark">...</div>
```

### 顶部导航

```html
<!-- 高度 56px -->
<header class="h-14 bg-white shadow-sm">...</header>

<!-- 用户头像: 渐变 + 圆角 -->
<div class="bg-gradient-to-br from-primary to-primary-dark rounded-full">
  ...
</div>
```

---

## 三、组件样式

### 按钮

```html
<!-- 主按钮: 渐变 + 悬停阴影 -->
<button
  class="bg-gradient-to-r from-primary to-primary-dark 
                hover:shadow-lg transition-shadow"
>
  主按钮
</button>

<!-- 次要按钮 -->
<button class="bg-gray-100 hover:bg-gray-200 rounded-lg">次要</button>

<!-- 危险按钮 -->
<button class="bg-danger text-white rounded-lg hover:bg-red-600">删除</button>
```

### 输入框

```html
<input
  class="rounded-xl border border-gray-300 
               focus:border-primary focus:ring-2 
               focus:ring-primary/15 px-4 py-2"
/>
```

### 表格

```html
<table class="w-full bg-white rounded-xl overflow-hidden">
  <thead class="bg-bg-subtle">
  <tr class="hover:bg-bg-hover transition-colors">
  <tr class="odd:bg-white even:bg-gray-50">
</table>
```

### 卡片

```html
<div
  class="bg-white rounded-2xl shadow-md hover:shadow-lg transition-shadow"
></div>
```

---

## 四、阴影系统

| 级别 | Tailwind    | 用途        |
| ---- | ----------- | ----------- |
| sm   | `shadow-sm` | 基础阴影    |
| md   | `shadow-md` | 卡片阴影    |
| lg   | `shadow-lg` | 悬停/对话框 |
| xl   | `shadow-xl` | 模态框      |

---

## 五、圆角系统

| 用途        | Tailwind      | 像素 |
| ----------- | ------------- | ---- |
| 按钮/输入框 | `rounded-lg`  | 8px  |
| 卡片        | `rounded-xl`  | 12px |
| 对话框      | `rounded-2xl` | 16px |

---

## 六、动画过渡

```html
<!-- 基础过渡 -->
<div class="transition-all duration-200">
  <!-- 卡片悬停: 上浮 + 阴影 -->
  <div class="hover:-translate-y-0.5 hover:shadow-lg"></div>
</div>
```

---

## 七、菜单样式

```html
<nav class="space-y-1">
  <a
    href="#"
    class="flex items-center gap-3 px-3 py-2 
                     rounded-lg hover:bg-gray-100"
  >
    <i class="fas fa-th-large w-5"></i>
  </a>

  <!-- 选中项 -->
  <a
    href="#"
    class="flex items-center gap-3 px-3 py-2 
                     rounded-lg bg-primary/10 text-primary"
  >
  </a>
</nav>
```

### 菜单图标

```
仪表盘: fa-th-large
商品管理: fa-box
客户管理: fa-users
销售管理: fa-shopping-cart
数据统计: fa-chart-bar
系统设置: fa-cog
```

---

## 八、页面结构

```html
<!-- 页面标题区 -->
<div class="flex items-center justify-between mb-6">
  <div>
    <h1 class="text-2xl font-bold">商品管理</h1>
    <p class="text-gray-500 mt-1">管理商品信息</p>
  </div>
  <button class="bg-primary text-white px-4 py-2 rounded-lg">新增</button>
</div>

<!-- 搜索区域 -->
<div class="bg-white rounded-xl shadow-sm p-4 mb-6">
  <div class="flex gap-4">
    <input class="flex-1 rounded-xl border px-4" />
    <button class="bg-primary text-white px-6 rounded-xl">搜索</button>
  </div>
</div>

<!-- 表格 -->
<div class="bg-white rounded-xl shadow-sm overflow-hidden">
  <table class="w-full">
    ...
  </table>
</div>
```

---

## 九、图标

使用 Font Awesome 6.4.0

```html
<link
  rel="stylesheet"
  href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
/>
<i class="fas fa-user"></i>
```

---

## 十、响应式

### Dashboard 统计卡片

```html
<!-- 大屏 4列, 中屏 2列, 小屏 1列 -->
<div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6"></div>
```

---

## 十一、开发注意事项

1. **动画**: 使用 `transition-all duration-200`
2. **卡片悬停**: `hover:-translate-y-0.5 hover:shadow-lg`
3. **按钮悬停**: 渐变 + 阴影变化
4. **输入框 Focus**: `focus:ring-2 focus:ring-primary/15`
5. **表格**: 使用 `divide-y` 分隔行，斑马纹用 `odd:`/`even:`
6. **布局**: 不使用 `max-width` 限制内容区宽度
