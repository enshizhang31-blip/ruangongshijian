# Web管理端 - 统计报表模块 API

> 关联文档：[需求详细设计.md - 第八章数据埋点模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 小程序埋点模块](../2026-03-28-数据库设计.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/statistics/dashboard` | 仪表盘统计 | statistics:view |
| GET | `/api/admin/statistics/sales` | 销售统计 | statistics:view |
| GET | `/api/admin/statistics/goods` | 商品统计 | statistics:view |
| GET | `/api/admin/statistics/customer` | 客户统计 | statistics:view |
| GET | `/api/admin/statistics/page` | 页面访问统计 | statistics:view |
| GET | `/api/admin/statistics/retained` | 用户留存统计 | statistics:view |

---

## GET /api/admin/statistics/dashboard - 仪表盘统计

### 功能说明

获取管理后台仪表盘的核心统计数据。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dateRange | string | 否 | 日期范围：today, week, month, year，默认 today |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "today": {
      "salesAmount": 13298.10,
      "orderCount": 5,
      "newCustomerCount": 2,
      "pageViews": 156
    },
    "compare": {
      "salesAmountChange": 15.5,
      "orderCountChange": 20.0,
      "newCustomerCountChange": -5.0,
      "pageViewsChange": 10.2
    },
    "trend": [
      {"date": "2026-03-23", "salesAmount": 10000, "orderCount": 4},
      {"date": "2026-03-24", "salesAmount": 12000, "orderCount": 5},
      {"date": "2026-03-25", "salesAmount": 8000, "orderCount": 3},
      {"date": "2026-03-26", "salesAmount": 15000, "orderCount": 6},
      {"date": "2026-03-27", "salesAmount": 11000, "orderCount": 4},
      {"date": "2026-03-28", "salesAmount": 14000, "orderCount": 5},
      {"date": "2026-03-29", "salesAmount": 13298.10, "orderCount": 5}
    ],
    "topGoods": [
      {"id": 1, "name": "iPhone 15 Pro", "orderCount": 10, "salesAmount": 69990},
      {"id": 2, "name": "MacBook Pro", "orderCount": 5, "salesAmount": 75000}
    ],
    "recentOrders": [
      {
        "id": 123,
        "orderNo": "ORD2026032900001",
        "customerName": "张三",
        "payAmount": 13298.10,
        "status": 1,
        "createdAt": "2026-03-29 10:00:00"
      }
    ]
  }
}
```

### 字段说明

| 字段 | 说明 |
|------|------|
| today | 今日数据 |
| compare | 较上期变化百分比 |
| trend | 近7天趋势 |
| topGoods | 热销商品TOP5 |
| recentOrders | 最近订单5条 |

---

## GET /api/admin/statistics/sales - 销售统计

### 功能说明

获取销售数据的详细统计，支持按时间维度分析。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| groupBy | string | 否 | 分组维度：day, week, month，默认 day |
| categoryId | long | 否 | 分类ID（筛选特定分类） |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalAmount": 699900.00,
      "orderCount": 50,
      "avgOrderAmount": 13998.00,
      "refundAmount": 5000.00,
      "refundRate": 0.71
    },
    "trend": [
      {
        "date": "2026-03-01",
        "salesAmount": 50000.00,
        "orderCount": 10,
        "refundAmount": 1000.00
      },
      {
        "date": "2026-03-02",
        "salesAmount": 45000.00,
        "orderCount": 8,
        "refundAmount": 0.00
      }
    ],
    "byPayType": [
      {"payType": 1, "payTypeName": "余额支付", "amount": 699900.00, "count": 50}
    ],
    "byStatus": [
      {"status": 0, "statusName": "待付款", "count": 5, "amount": 69990.00},
      {"status": 1, "statusName": "已付款", "count": 40, "amount": 559920.00},
      {"status": 2, "statusName": "已完成", "count": 35, "amount": 489930.00},
      {"status": 5, "statusName": "已退款", "count": 5, "amount": 5000.00}
    ]
  }
}
```

---

## GET /api/admin/statistics/goods - 商品统计

### 功能说明

获取商品维度的统计数据，包括访问量、加购量、下单量。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| sort | string | 否 | 排序：pv, cart, order，默认 order |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "goodsId": 1,
        "goodsName": "iPhone 15 Pro",
        "categoryName": "手机",
        "pv": 1560,
        "uv": 450,
        "cartCount": 89,
        "orderCount": 45,
        "conversionRate": 10.0,
        "salesAmount": 314955.00
      },
      {
        "goodsId": 2,
        "goodsName": "MacBook Pro",
        "categoryName": "电脑",
        "pv": 980,
        "uv": 320,
        "cartCount": 56,
        "orderCount": 28,
        "conversionRate": 8.75,
        "salesAmount": 420000.00
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100
    }
  }
}
```

### 字段说明

| 字段 | 说明 |
|------|------|
| pv | 访问次数（Page View） |
| uv | 访问人数（Unique Visitor） |
| cartCount | 加购次数 |
| orderCount | 下单次数 |
| conversionRate | 转化率 = orderCount / uv * 100% |
| salesAmount | 销售金额 |

---

## GET /api/admin/statistics/customer - 客户统计

### 功能说明

获取客户维度的统计数据，包括新增、活跃、留存等。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| groupBy | string | 否 | 分组维度：day, week, month，默认 day |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalCustomer": 1000,
      "activeCustomer": 450,
      "newCustomer": 50,
      "vipCustomer": 150
    },
    "trend": [
      {
        "date": "2026-03-01",
        "newCustomer": 10,
        "activeCustomer": 80,
        "vipCustomer": 20
      }
    ],
    "byLevel": [
      {"level": 1, "levelName": "普通", "count": 550},
      {"level": 2, "levelName": "银卡", "count": 300},
      {"level": 3, "levelName": "金卡", "count": 120},
      {"level": 4, "levelName": "钻石", "count": 30}
    ],
    "bySource": [
      {"source": "wechat", "count": 900},
      {"source": "app", "count": 100}
    ]
  }
}
```

---

## GET /api/admin/statistics/page - 页面访问统计

### 功能说明

获取小程序端各页面的访问数据。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |
| sort | string | 否 | 排序：pv, uv, duration，默认 pv |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "pageUrl": "/pages/index/index",
      "pageTitle": "首页",
      "pv": 5000,
      "uv": 1500,
      "avgDuration": 45.5
    },
    {
      "pageUrl": "/pages/goods/detail",
      "pageTitle": "商品详情",
      "pv": 3500,
      "uv": 1200,
      "avgDuration": 120.3
    },
    {
      "pageUrl": "/pages/cart/index",
      "pageTitle": "购物车",
      "pv": 2000,
      "uv": 800,
      "avgDuration": 30.2
    }
  ]
}
```

---

## GET /api/admin/statistics/retained - 用户留存统计

### 功能说明

获取用户留存率数据。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | date | 否 | 开始日期（最大90天前） |
| endDate | date | 否 | 结束日期 |
| period | string | 否 | 留存周期：1d, 3d, 7d, 30d，默认 7d |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "period": "7d",
    "list": [
      {
        "statDate": "2026-03-22",
        "newUsers": 50,
        "retainedUsers": 35,
        "retainedRate": 70.0
      },
      {
        "statDate": "2026-03-21",
        "newUsers": 45,
        "retainedUsers": 28,
        "retainedRate": 62.2
      }
    ],
    "summary": {
      "avg1dRate": 65.0,
      "avg3dRate": 50.0,
      "avg7dRate": 45.0,
      "avg30dRate": 30.0
    }
  }
}
```

### 留存率计算

```
次日留存率 = 第N天回访用户数 / 第N天新增用户数 * 100%
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 备注

1. 统计数据每日凌晨计算前一天的汇总数据
2. 实时数据从原始日志统计，可能有延迟
3. 销售金额基于实际支付金额，不含取消订单
4. 页面停留时长单位为秒
5. 转化率 = 下单人数 / 访问人数 * 100%