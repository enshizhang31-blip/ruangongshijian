import { http } from '@/utils/request.js'

// 认证
export const authApi = {
    /** 微信登录（保留） */
    login(code) { return http.post('/auth/login', { code }) },
    /** 演示版：手机号 + 密码登录 */
    loginByPhone(phone, password) { return http.post('/auth/login-by-phone', { phone, password }) },
    /** 演示版：手机号 + 密码注册 */
    register(phone, password, nickname) { return http.post('/auth/register', { phone, password, nickname }) }
}

// 分类（公开）
export const categoryApi = {
    list(parentId) { return http.get('/category', { parentId }) }
}

// SPU商品（公开）
export const spuApi = {
    list(params) { return http.get('/spu', params) },
    detail(id) { return http.get(`/spu/${id}`) }
}

// 购物车
export const cartApi = {
    list() { return http.get('/cart') },
    add(data) { return http.post('/cart', data) },
    updateQty(id, quantity) { return http.put(`/cart/${id}`, { quantity }) },
    select(id, selected) { return http.put(`/cart/${id}`, { selected }) },
    remove(id) { return http.delete(`/cart/${id}`) },
    clear() { return http.delete('/cart') }
}

// 订单
export const orderApi = {
    create(data) { return http.post('/order', data) },
    list(params) { return http.get('/order', params) },
    detail(id) { return http.get(`/order/${id}`) },
    pay(id, data) { return http.post(`/order/${id}/pay`, data) },
    cancel(id) { return http.post(`/order/${id}/cancel`) },
    receive(id) { return http.post(`/order/${id}/receive`) },
    refund(id, data) { return http.post(`/order/${id}/refund`, data) }
}

// 会员
export const memberApi = {
    info() { return http.get('/member/info') },
    updateProfile(data) { return http.put('/member/info', data) },
    balance() { return http.get('/member/balance') },
    recharge(data) { return http.post('/member/recharge', data) },
    points() { return http.get('/member/points') },
    pointsHistory(params) { return http.get('/member/points/history', params) }
}

// 收货地址
export const addressApi = {
    list() { return http.get('/address') },
    detail(id) { return http.get(`/address/${id}`) },
    create(data) { return http.post('/address', data) },
    update(id, data) { return http.put(`/address/${id}`, data) },
    remove(id) { return http.delete(`/address/${id}`) },
    setDefault(id) { return http.put(`/address/${id}/default`) }
}

// 行为埋点
export const behaviorApi = {
    log(events) { return http.post('/behavior/log', { events }) }
}

// SN码（扫码用）
export const snApi = {
    query(snCode) { return http.get(`/sn/${snCode}`) }
}

// 国际化（后端公开端点）
export const i18nApi = {
    getLocales() { return http.get('/i18n/locales') }
}
