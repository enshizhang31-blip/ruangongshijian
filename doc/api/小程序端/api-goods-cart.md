# 小程序端 - 商品与购物车模块 API

> 关联文档：[需求详细设计.md](../2026-03-27-需求详细设计.md)、[数据库设计.md](../2026-03-28-数据库设计.md)、[国际化方案.md](../2026-03-29-国际化方案.md)

---

## 接口列表

| 方法 | 路径 | 说明 | 权限 | 国际化 |
|------|------|------|------|--------|
| GET | `/api/category` | 分类列表 | 无 | ✅ |
| GET | `/api/spu` | SPU商品列表 | 无 | ✅ |
| GET | `/api/spu/{id}` | SPU商品详情 | 无 | ✅ |
| GET | `/api/cart` | 购物车列表 | 需登录 | - |
| POST | `/api/cart` | 添加商品到购物车 | 需登录 | - |
| PUT | `/api/cart/{id}` | 修改购物车商品数量 | 需登录 | - |
| DELETE | `/api/cart/{id}` | 删除购物车商品 | 需登录 | - |
| DELETE | `/api/cart` | 清空购物车 | 需登录 | - |

---

## GET /api/category - 分类列表

### 功能说明

获取商品分类树形结构，支持多语言返回。

### 请求

- Method: `GET`
- Headers: `X-Locale: zh-CN` (可选，默认 zh-CN)

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | long | 否 | 父级分类ID，空表示获取顶级分类 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "电子产品",
      "parentId": 0,
      "icon": "https://xxx.com/icon.png",
      "sort": 1,
      "children": [
        {
          "id": 2,
          "name": "手机",
          "parentId": 1,
          "icon": "https://xxx.com/phone.png",
          "sort": 1
        }
      ]
    }
  ]
}
```

---

## GET /api/spu - SPU商品列表

### 功能说明

获取商品列表，支持分类筛选、搜索、分页。返回的商品信息包含多语言翻译和动态计算的价格。

### 请求

- Method: `GET`
- Headers: `X-Locale: zh-CN` (可选)

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | long | 否 | 分类ID |
| keyword | string | 否 | 搜索关键词（匹配名称） |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| sort | string | 否 | 排序：price_asc/price_desc/sales/new |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "iPhone 15 Pro",
        "categoryId": 2,
        "categoryName": "手机",
        "brand": "Apple",
        "imageUrl": "https://xxx.com/iphone15.jpg",
        "images": ["https://xxx.com/1.jpg", "https://xxx.com/2.jpg"],
        "description": "最新款iPhone",
        "status": 1,
        "minPrice": 6999.00,
        "currency": "CNY",
        "skus": [
          {
            "id": 101,
            "skuCode": "IP15-BLK-256",
            "specJson": {"颜色": "黑色", "内存": "256G"},
            "price": 6999.00,
            "currency": "CNY",
            "stock": 50
          }
        ],
        "tags": ["热销", "新品"]
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

### 价格计算说明

当 `X-Locale` 为非中文时，价格根据汇率动态计算：

```
1. 从 Redis 获取汇率：exchange_rate:CNY:{currency}
2. 计算：price = originPrice × rate
3. 返回 price 和 currency
```

---

## GET /api/spu/{id} - SPU商品详情

### 请求

- Method: `GET`
- Headers: `X-Locale: zh-CN` (可选)

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "categoryId": 2,
    "categoryName": "手机",
    "brand": "Apple",
    "imageUrl": "https://xxx.com/iphone15.jpg",
    "images": ["https://xxx.com/1.jpg", "https://xxx.com/2.jpg"],
    "description": "苹果最新款智能手机，采用A17 Pro芯片...",
    "status": 1,
    "skus": [
      {
        "id": 101,
        "skuCode": "IP15-BLK-256",
        "specJson": {"颜色": "黑色", "内存": "256G"},
        "price": 6999.00,
        "currency": "CNY",
        "costPrice": 5500.00,
        "imageUrl": "https://xxx.com/black.jpg",
        "stock": 50,
        "customAttrs": [
          {
            "fieldKey": "material",
            "fieldType": "text",
            "fieldName": "材质",
            "value": "钛金属"
          },
          {
            "fieldKey": "battery",
            "fieldType": "select",
            "fieldName": "电池容量",
            "valueId": "2",
            "value": "3000mAh"
          }
        ]
      }
    ],
    "specs": [
      {
        "name": "颜色",
        "values": ["黑色", "白色", "蓝色"]
      },
      {
        "name": "内存",
        "values": ["128G", "256G", "512G"]
      }
    ]
  }
}
```

---

## GET /api/cart - 购物车列表

### 功能说明

获取当前会员的购物车商品列表。

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "skuId": 101,
        "spuName": "iPhone 15 Pro",
        "skuSpec": "黑色 / 256G",
        "skuImage": "https://xxx.com/iphone.jpg",
        "price": 6999.00,
        "currency": "CNY",
        "quantity": 2,
        "subtotal": 13998.00,
        "maxQuantity": 50
      }
    ],
    "totalAmount": 13998.00,
    "currency": "CNY",
    "discountAmount": 0,
    "payAmount": 13998.00
  }
}
```

---

## POST /api/cart - 添加商品到购物车

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skuId | long | 是 | SKU ID |
| quantity | int | 是 | 数量（至少1） |

**请求示例：**

```json
{
  "skuId": 101,
  "quantity": 1
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cartItemCount": 5
  }
}
```

### 业务逻辑

1. 检查 SKU 是否存在且状态正常
2. 检查库存是否充足
3. 如购物车已有同 SKU，增加数量；否则新增记录

---

## PUT /api/cart/{id} - 修改购物车商品数量

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| quantity | int | 是 | 新数量（至少1，0表示删除） |

**请求示例：**

```json
{
  "quantity": 3
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "quantity": 3,
    "subtotal": 20997.00
  }
}
```

---

## DELETE /api/cart/{id} - 删除购物车商品

### 请求

- Method: `DELETE`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## DELETE /api/cart - 清空购物车

### 功能说明

清空当前会员的所有购物车商品。

### 请求

- Method: `DELETE`
- Headers: `Authorization: Bearer {token}`

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 404 | 商品/SKU不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 3001 | 库存不足 |
| 3002 | 商品已下架 |
| 3003 | SKU已禁用 |

---

## 备注

1. 购物车以 SKU 为单位，同一 SKU 多次添加会累加数量
2. 商品价格根据 Header `X-Locale` 动态计算币种
3. 库存显示的是该 SKU 下所有"在库"状态的 SN 码数量