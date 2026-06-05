import { http } from '@/utils/request'

export const goodsApi = {
    list(params) { return http.get('/goods', params) },
    detail(id) { return http.get(`/goods/${id}`) }
}

export const categoryApi = {
    list() { return http.get('/categories') }
}

export const cartApi = {
    list() { return http.get('/cart') },
    add(data) { return http.post('/cart', data) },
    updateQty(id, quantity) { return http.put(`/cart/${id}`, { quantity }) },
    remove(id) { return http.delete(`/cart/${id}`) },
    clear() { return http.delete('/cart') }
}

export const orderApi = {
    create(data) { return http.post('/order', data) },
    list(params) { return http.get('/order', params) },
    detail(id) { return http.get(`/order/${id}`) },
    pay(id) { return http.post(`/order/${id}/pay`) },
    cancel(id) { return http.post(`/order/${id}/cancel`) },
    refund(id) { return http.post(`/order/${id}/refund`) }
}

export const memberApi = {
    info() { return http.get('/member/info') },
    balance() { return http.get('/member/balance') },
    recharge(data) { return http.post('/member/recharge', data) },
    points() { return http.get('/member/points') },
    pointsHistory() { return http.get('/member/points/history') }
}

export const addressApi = {
    list() { return http.get('/address') },
    create(data) { return http.post('/address', data) },
    update(id, data) { return http.put(`/address/${id}`, data) },
    remove(id) { return http.delete(`/address/${id}`) },
    setDefault(id) { return http.put(`/address/${id}/default`) }
}

export const i18nApi = {
    getLocales() { return http.get('/i18n/locales') }
}
