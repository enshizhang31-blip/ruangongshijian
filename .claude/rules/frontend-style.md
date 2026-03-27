# looi-admin-web 前端 UI 设计规范

本文档记录 looi-admin-web 项目的 UI 风格，用于后续新页面开发参考。

---

## 一、配色系统

### 主色调
```scss
--color-primary: #6366f1;        // 品牌紫 (Indigo)
--color-primary-light: #818cf8;  // 浅紫色
--color-primary-dark: #4f46e5;  // 深紫色
```

### 功能色
```scss
--color-success: #10b981;   // 绿色
--color-warning: #f59e0b;    // 黄色
--color-danger: #ef4444;     // 红色
--color-info: #3b82f6;      // 蓝色
```

### 背景色
```scss
--color-bg-page: #f9fafb;    // 页面背景
--color-bg-main: #ffffff;     // 卡片/内容区背景
--color-bg-subtle: #f3f4f6;  // 表格头部/次要背景
--color-bg-hover: #e5e7eb;    // 悬停背景
```

### 边框色
```scss
--color-border: #d1d5db;
--color-border-light: #e5e7eb;
```

---

## 二、布局结构

### 侧边栏
- 宽度: 260px (展开) / 72px (折叠)
- 背景: 白色 + 阴影 (box-shadow)
- Logo区: 渐变背景 (linear-gradient 135deg)
- 菜单项:
  - 子菜单选中: 浅紫色背景 rgba(99, 102, 241, 0.1) + 主题色文字
  - 父级菜单选中: 保持普通样式，不变亮

### 顶部导航
- 高度: 56px
- 背景: 白色 + 底部阴影
- 用户头像: 渐变背景 + 圆角

### 内容区
- 内边距: 24px 32px (上下 左右)
- 无最大宽度限制，撑满可用空间

---

## 三、组件样式

### 按钮
- 圆角: var(--radius-md) = 8px
- 主按钮: 渐变背景 + 悬停阴影效果
- 文字按钮: link 类型

### 输入框
- 圆角: var(--radius-lg) = 12px
- Focus: 紫色边框 + 紫色光晕 (0 0 0 3px rgba(99, 102, 241, 0.15))

### 表格
- 圆角: var(--radius-lg) = 12px
- 单元格内边距: 12px 16px
- 表头背景: var(--color-bg-subtle)
- 悬停行: #f9fafb
- 斑马纹: stripe

### 对话框
- 圆角: var(--radius-xl) = 16px
- 阴影: var(--shadow-xl)
- 头部/底部: 有边框分隔

### 标签 (Tag)
- 圆角: var(--radius-sm) = 6px

### 分页
- 圆角: var(--radius-sm)
- 激活页: 主题色背景

---

## 四、阴影系统

```scss
--shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
```

---

## 五、圆角系统

```scss
--radius-sm: 6px;
--radius-md: 8px;
--radius-lg: 12px;
--radius-xl: 16px;
```

使用场景:
- 按钮/输入框: md / lg
- 卡片: xl
- 对话框: xl

---

## 六、动画过渡

```scss
--transition-fast: 150ms ease;
--transition-base: 200ms ease;
```

### 页面动画
- 页面加载: fadeIn (淡入 + 向上位移)
- 卡片悬停: translateY(-2px) + shadow 增强
- 按钮悬停: 轻微上浮 + 阴影

---

## 七、菜单样式

### 侧边栏菜单
- 菜单图标: Font Awesome
- 子菜单选中: 浅紫色背景 + 主题色文字
- 父级菜单选中: 不变亮
- 菜单项间距: margin: 2px 4px

### 菜单图标示例
```html
<span class="menu-icon"><i class="fas fa-th-large"></i></span>
仪表盘: fa-th-large
系统管理: fa-cog
用户管理: fa-user
角色管理: fa-users
菜单管理: fa-bars
指令管理: fa-terminal
多语言管理: fa-language
```

---

## 八、页面结构

### 列表页结构
1. 页面标题区 (page-header)
   - 标题 + 描述
   - 新增按钮 (右上角)

2. 搜索区域 (search-card)
   - 白色卡片背景
   - 圆角 + 阴影
   - flex 布局，输入框自适应宽度

3. 表格区域 (table-card)
   - 白色背景
   - 圆角 + 阴影
   - 无内边距 (表格自带)

4. 分页区域
   - 底部边框分隔
   - 右对齐

### 列宽设置
- 使用 min-width 替代固定 width
- 状态/操作列添加 align="center"

---

## 九、图标

使用 Element Plus Icons 和 Font Awesome 6.4.0

引入方式:
```html
<!-- Font Awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
```

---

## 十、字体

```scss
--font-sans: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Noto Sans SC', sans-serif;
```

---

## 十一、响应式

### Dashboard 统计卡片
- 大屏 (≥1400px): 4列
- 中屏 (768-1400px): 2列
- 小屏 (<768px): 1列

---

## 十二、开发注意事项

1. 所有页面添加动画: `animation: fadeIn var(--transition-base) ease-out`
2. 卡片悬停效果: `:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }`
3. 按钮悬停效果: 渐变 + 阴影
4. 输入框 focus 效果: 紫色光晕
5. 表格使用 stripe + hover 效果
6. 侧边栏菜单图标使用 Font Awesome
7. 页面布局不使用 max-width 限制，撑满空间
