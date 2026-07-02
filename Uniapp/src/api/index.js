// API 聚合，所有调用都走 ./utils/request.js，token 自动注入
import request from '@/utils/request'

// ========== 认证 ==========
export const authApi = {
  loginByPhone(phone, password) {
    return request({
      url: '/api/auth/login-by-phone',
      method: 'POST',
      data: { phone, password }
    })
  },
  login() {
    return request({
      url: '/api/auth/login',
      method: 'POST',
      data: {}
    })
  },
  register(phone, password, nickname) {
    return request({
      url: '/api/auth/register',
      method: 'POST',
      data: { phone, password, nickname }
    })
  }
}

// ========== 多语言 ==========
export const i18nApi = {
  list() {
    return request({
      url: '/api/i18n/locales',
      method: 'GET'
    })
  }
}

// ========== 分类 / 商品 ==========
export const categoryApi = {
  tree() {
    return request({ url: '/category/tree', method: 'GET' })
  },
  list() {
    return request({ url: '/category/list', method: 'GET' })
  }
}

export const spuApi = {
  list({ keyword, categoryId, page = 1, pageSize = 20 } = {}) {
    return request({
      url: '/spu/list',
      method: 'GET',
      data: { keyword, categoryId, page, pageSize }
    })
  },
  detail(id) {
    return request({ url: '/spu/detail/' + id, method: 'GET' })
  }
}

export const skuApi = {
  list(spuId) {
    return request({ url: '/sku/list', method: 'GET', data: { spuId } })
  },
  detail(id) {
    return request({ url: '/sku/detail/' + id, method: 'GET' })
  }
}

// ========== 购物车 ==========
export const cartApi = {
  list() {
    return request({ url: '/cart/list', method: 'GET' })
  },
  add({ spuId, skuId, quantity }) {
    return request({
      url: '/cart/add',
      method: 'POST',
      data: { spuId, skuId, quantity }
    })
  },
  update({ id, quantity, selected }) {
    return request({
      url: '/cart/update',
      method: 'POST',
      data: { id, quantity, selected }
    })
  },
  remove(id) {
    return request({ url: '/cart/remove/' + id, method: 'DELETE' })
  },
  selectAll(selected) {
    return request({
      url: '/cart/select-all',
      method: 'POST',
      data: { selected }
    })
  }
}

// ========== 订单 ==========
export const orderApi = {
  list({ status, page = 1, pageSize = 20 } = {}) {
    return request({
      url: '/order/list',
      method: 'GET',
      data: { status, page, pageSize }
    })
  },
  detail(id) {
    return request({ url: '/order/detail/' + id, method: 'GET' })
  },
  create({ addressId, items }) {
    return request({
      url: '/order/create',
      method: 'POST',
      data: { addressId, items }
    })
  },
  pay(orderId, payType) {
    return request({
      url: '/order/pay',
      method: 'POST',
      data: { orderId, payType }
    })
  },
  cancel(orderId) {
    return request({
      url: '/order/cancel',
      method: 'POST',
      data: { orderId }
    })
  },
  confirmReceive(orderId) {
    return request({
      url: '/order/confirm-receive',
      method: 'POST',
      data: { orderId }
    })
  },
  ship(orderId) {
    return request({
      url: '/order/ship',
      method: 'POST',
      data: { orderId }
    })
  },
  refundComplete(orderId) {
    return request({
      url: '/order/refund-complete',
      method: 'POST',
      data: { orderId }
    })
  },
  refund(orderId, reason) {
    return request({
      url: '/order/refund',
      method: 'POST',
      data: { orderId, reason }
    })
  }
}

// ========== 地址 ==========
export const addressApi = {
  list() {
    return request({ url: '/address/list', method: 'GET' })
  },
  create(payload) {
    return request({ url: '/address/create', method: 'POST', data: payload })
  },
  update(id, payload) {
    return request({ url: '/address/update/' + id, method: 'POST', data: payload })
  },
  remove(id) {
    return request({ url: '/address/remove/' + id, method: 'DELETE' })
  },
  setDefault(id) {
    return request({ url: '/address/default/' + id, method: 'POST' })
  }
}

// ========== 会员 ==========
export const memberApi = {
  info() {
    return request({ url: '/member/info', method: 'GET' })
  }
}

// ========== SN 公开查询 ==========
export const snApi = {
  query({ sn, skuId } = {}) {
    return request({ url: '/sn/query', method: 'GET', data: { sn, skuId } })
  }
}
