# Web管理端 - 商品管理模块 API

> 关联文档：[需求详细设计.md - 第二章商品模块](../2026-03-27-需求详细设计.md)、[数据库设计.md - 商品模块](../2026-03-28-数据库设计.md)、[国际化方案.md](../2026-03-29-国际化方案.md)

---

## 接口列表

### SPU管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/spu` | SPU列表（分页筛选） | spu:view |
| GET | `/api/admin/spu/{id}` | SPU详情 | spu:view |
| POST | `/api/admin/spu` | 新增SPU | spu:add |
| PUT | `/api/admin/spu/{id}` | 编辑SPU | spu:edit |
| DELETE | `/api/admin/spu/{id}` | 删除SPU | spu:delete |
| PUT | `/api/admin/spu/{id}/status` | 修改状态（上架/下架） | spu:edit |

### SKU管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/spu/{spuId}/sku` | 获取SPU下的所有SKU | spu:view |
| POST | `/api/admin/sku` | 新增SKU | sku:add |
| PUT | `/api/admin/sku/{id}` | 编辑SKU | sku:edit |
| DELETE | `/api/admin/sku/{id}` | 删除SKU | sku:delete |

### 分类管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/category` | 分类树 | category:view |
| POST | `/api/admin/category` | 新增分类 | category:add |
| PUT | `/api/admin/category/{id}` | 编辑分类 | category:edit |
| DELETE | `/api/admin/category/{id}` | 删除分类 | category:delete |

### 规格管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/spec` | 规格列表 | spec:view |
| POST | `/api/admin/spec` | 新增规格名称 | spec:add |
| PUT | `/api/admin/spec/{id}` | 编辑规格名称 | spec:edit |
| DELETE | `/api/admin/spec/{id}` | 删除规格名称 | spec:delete |
| GET | `/api/admin/spec/{id}/value` | 规格值列表 | spec:view |
| POST | `/api/admin/spec/{id}/value` | 新增规格值 | spec:add |
| PUT | `/api/admin/spec/value/{id}` | 编辑规格值 | spec:edit |
| DELETE | `/api/admin/spec/value/{id}` | 删除规格值 | spec:delete |

### 多语言翻译

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/goods/{id}/i18n` | 获取商品所有翻译 | spu:view |
| PUT | `/api/admin/goods/{id}/i18n/{locale}` | 保存商品翻译（单语言） | spu:edit |
| PUT | `/api/admin/goods/{id}/i18n` | 批量保存翻译 | spu:edit |
| GET | `/api/admin/category/{id}/i18n` | 获取分类翻译 | category:view |
| PUT | `/api/admin/category/{id}/i18n` | 保存分类翻译 | category:edit |

---

## SPU管理接口

### GET /api/admin/spu - SPU列表

### 请求

- Method: `GET`
- Headers: `Authorization: Bearer {token}`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | long | 否 | 分类ID |
| keyword | string | 否 | 搜索关键词 |
| status | int | 否 | 状态：0下架 1上架 |
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
        "id": 1,
        "name": "iPhone 15 Pro",
        "categoryId": 2,
        "categoryName": "手机",
        "brand": "Apple",
        "imageUrl": "https://xxx.com/iphone15.jpg",
        "status": 1,
        "statusName": "上架",
        "skuCount": 3,
        "createdAt": "2026-03-01 10:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50
    }
  }
}
```

---

### POST /api/admin/spu - 新增SPU

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | SPU名称 |
| categoryId | long | 是 | 分类ID |
| brand | string | 否 | 品牌 |
| imageUrl | string | 否 | 主图片URL |
| images | array | 否 | 多图片URL列表 |
| description | string | 否 | 商品描述 |

**请求示例：**

```json
{
  "name": "iPhone 15 Pro",
  "categoryId": 2,
  "brand": "Apple",
  "imageUrl": "https://xxx.com/iphone15.jpg",
  "images": ["https://xxx.com/1.jpg", "https://xxx.com/2.jpg"],
  "description": "苹果最新款智能手机"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "createdAt": "2026-03-29 10:00:00"
  }
}
```

---

### PUT /api/admin/spu/{id} - 编辑SPU

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | SPU名称 |
| categoryId | long | 是 | 分类ID |
| brand | string | 否 | 品牌 |
| imageUrl | string | 否 | 主图片URL |
| images | array | 否 | 多图片URL列表 |
| description | string | 否 | 商品描述 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### PUT /api/admin/spu/{id}/status - 修改状态

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | int | 是 | 状态：0下架 1上架 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### DELETE /api/admin/spu/{id} - 删除SPU

### 功能说明

删除SPU会级联删除关联的SKU和SN码。

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

## SKU管理接口

### GET /api/admin/spu/{spuId}/sku - 获取SPU下的SKU

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 101,
      "skuCode": "IP15-BLK-256",
      "specJson": {"颜色": "黑色", "内存": "256G"},
      "price": 6999.00,
      "costPrice": 5500.00,
      "unit": "件",
      "imageUrl": "https://xxx.com/black.jpg",
      "status": 1,
      "stock": 50
    }
  ]
}
```

---

### POST /api/admin/sku - 新增SKU

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | long | 是 | SPU ID |
| skuCode | string | 是 | SKU编码（唯一） |
| specJson | object | 是 | 规格JSON，如 {"颜色": "黑色", "内存": "256G"} |
| price | decimal | 是 | 销售价格 |
| costPrice | decimal | 否 | 成本价 |
| unit | string | 否 | 单位，默认"件" |
| imageUrl | string | 否 | SKU图片 |

**请求示例：**

```json
{
  "spuId": 1,
  "skuCode": "IP15-BLK-256",
  "specJson": {"颜色": "黑色", "内存": "256G"},
  "price": 6999.00,
  "costPrice": 5500.00,
  "unit": "件",
  "imageUrl": "https://xxx.com/black.jpg"
}
```

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 101
  }
}
```

---

## 分类管理接口

### GET /api/admin/category - 分类树

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
      "status": 1,
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

### POST /api/admin/category - 新增分类

### 请求

- Method: `POST`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 分类名称 |
| parentId | long | 否 | 父级ID，默认0 |
| icon | string | 否 | 图标URL |
| sort | int | 否 | 排序，默认0 |

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3
  }
}
```

---

## 规格管理接口

### GET /api/admin/spec - 规格列表

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "颜色",
      "sort": 1,
      "status": 1,
      "values": [
        {"id": 1, "value": "黑色", "sort": 1},
        {"id": 2, "value": "白色", "sort": 2}
      ]
    }
  ]
}
```

---

## 多语言翻译接口

### GET /api/admin/goods/{id}/i18n - 获取商品所有翻译

### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "goodsId": 1,
    "locales": {
      "zh-CN": {
        "name": "iPhone 15 Pro",
        "description": "苹果最新款智能手机",
        "shortDesc": "全新设计",
        "currency": "CNY",
        "customAttrs": []
      },
      "en-US": {
        "name": "iPhone 15 Pro",
        "description": "Latest iPhone with A17 Pro chip",
        "shortDesc": "New design",
        "currency": "USD",
        "customAttrs": []
      }
    }
  }
}
```

---

### PUT /api/admin/goods/{id}/i18n/{locale} - 保存商品翻译（单语言）

### 请求

- Method: `PUT`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `application/json`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 商品名称 |
| description | string | 否 | 商品描述 |
| shortDesc | string | 否 | 简短描述 |
| currency | string | 是 | 货币类型：CNY/USD/EUR/JPY |
| customAttrs | array | 否 | 自定义属性数组 |

**customAttrs 示例：**

```json
[
  {"fieldKey": "material", "fieldType": "text", "fieldName": "材质", "value": "钛金属"},
  {"fieldKey": "battery", "fieldType": "select", "fieldName": "电池容量", "valueId": "2", "value": "3000mAh"}
]
```

---

## 通用错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码

| code | 说明 |
|------|------|
| 2001 | SKU编码已存在 |
| 2002 | 分类下有商品，无法删除 |
| 2003 | 规格值被使用，无法删除 |

---

## 备注

1. SPU删除会级联删除所有关联的SKU和SN码，需谨慎操作
2. SKU编码在整个系统中唯一，建议格式：`{SPU简写}-{规格1}-{规格2}`
3. 多语言数据存储在MongoDB，MySQL存储基础数据
4. 自定义属性 fieldKey 必须与 field_definitions 中的定义一致