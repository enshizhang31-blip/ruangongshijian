<template>
    <view class="page">
        <view class="tabs">
            <view class="tab" :class="{ active: tab === 'all' }" @click="tab = 'all'">{{ $t('order.tabs.all') }}</view>
            <view class="tab" :class="{ active: tab === 'pending' }" @click="tab = 'pending'">{{ $t('order.tabs.pending') }}</view>
            <view class="tab" :class="{ active: tab === 'shipping' }" @click="tab = 'shipping'">{{ $t('order.tabs.shipping') }}</view>
            <view class="tab" :class="{ active: tab === 'done' }" @click="tab = 'done'">{{ $t('order.tabs.done') }}</view>
        </view>

        <view class="demo-bar" v-if="orders.length === 0">
            <view class="demo-btn" @click="seedDemoOrders">🎬 {{ $t('order.listTitle') }} Demo</view>
        </view>

        <view class="order-list">
            <view class="order-item" v-for="o in filtered" :key="o.id" @click="goDetail(o.id)">
                <view class="order-header">
                    <text>{{ $t('order.orderNo') }}: {{ o.orderNo }}</text>
                    <text class="status" :class="'s' + o.status">{{ $t('status.' + o.status) }}</text>
                </view>
                <view class="order-body" v-for="i in (o.items || [])" :key="i.id || i.spuId">
                    <image class="g-thumb" :src="i.imageUrl || '/static/logo.png'" mode="aspectFill" />
                    <view class="g-text">
                        <text class="g-name">{{ i.goodsName || i.spuName }}</text>
                        <text class="g-qty">×{{ i.quantity }}</text>
                    </view>
                    <text class="g-price">{{ $t('common.yuan') }}{{ formatPrice(i.price) }}</text>
                </view>
                <view class="order-footer">
                    <text>{{ $t('order.totalAmount') }}: <text class="amount">{{ $t('common.yuan') }}{{ formatPrice(o.totalAmount) }}</text></text>
                </view>
                <view class="shipping-tip" v-if="o.status === 2">🚚 {{ $t('logistics.transit') }}</view>
            </view>
        </view>
        <view v-if="filtered.length === 0 && orders.length > 0" class="empty">{{ $t('order.noOrder') }}</view>
        <view v-if="orders.length === 0" class="empty-tip">{{ $t('order.noOrder') }}</view>
    </view>
</template>

<script>
import { orderApi } from '@/api/index.js'

export default {
    data() { return { tab: 'all', orders: [], loading: false } },
    computed: {
        filtered() {
            if (this.tab === 'all') return this.orders
            if (this.tab === 'pending') return this.orders.filter(o => o.status === 0)
            if (this.tab === 'shipping') return this.orders.filter(o => o.status === 2 || o.status === 3)
            if (this.tab === 'done') return this.orders.filter(o => o.status >= 3 && o.status < 6)
            return this.orders
        }
    },
    onShow() { this.loadOrders() },
    methods: {
        async loadOrders() {
            this.loading = true
            const local = uni.getStorageSync('demo-orders')
            if (Array.isArray(local) && local.length) { this.orders = local; this.loading = false; return }
            try {
                const res = await orderApi.list({ page: 1, pageSize: 20 })
                this.orders = (res?.list || res || []).filter(o => true)
            } catch (e) { this.orders = [] }
            finally { this.loading = false }
        },
        seedDemoOrders() {
            const now = Date.now()
            const demo = [
                { id: now + 1, orderNo: 'D' + (now + 1), status: 0, totalAmount: 199.00, createdAt: '2026-07-02 10:00',
                  items: [{ id: 'a', goodsName: 'SaleManager 演示款 Pro', imageUrl: '/static/logo.png', quantity: 1, price: 199.00 }] },
                { id: now + 2, orderNo: 'D' + (now + 2), status: 1, totalAmount: 299.00, createdAt: '2026-07-02 09:30',
                  items: [{ id: 'b', goodsName: 'SaleManager 演示款 Plus', imageUrl: '/static/logo.png', quantity: 1, price: 299.00 }] },
                { id: now + 3, orderNo: 'D' + (now + 3), status: 2, totalAmount: 459.00, createdAt: '2026-07-01 18:20',
                  items: [{ id: 'c', goodsName: 'SaleManager 演示款 Ultra', imageUrl: '/static/logo.png', quantity: 1, price: 459.00 }] },
                { id: now + 4, orderNo: 'D' + (now + 4), status: 2, totalAmount: 599.00, createdAt: '2026-06-30 21:15',
                  items: [{ id: 'd', goodsName: 'SaleManager 演示款 Max', imageUrl: '/static/logo.png', quantity: 2, price: 299.50 }] },
                { id: now + 5, orderNo: 'D' + (now + 5), status: 3, totalAmount: 1280.00, createdAt: '2026-06-29 14:00',
                  items: [{ id: 'e', goodsName: 'SaleManager 演示款 Premium', imageUrl: '/static/logo.png', quantity: 4, price: 320.00 }] },
                { id: now + 6, orderNo: 'D' + (now + 6), status: 4, totalAmount: 999.00, createdAt: '2026-06-28 09:30',
                  items: [{ id: 'f', goodsName: 'SaleManager 演示款 Pro', imageUrl: '/static/logo.png', quantity: 1, price: 999.00 }] }
            ]
            uni.setStorageSync('demo-orders', demo)
            demo.forEach(o => uni.setStorageSync(`demo-order-${o.id}`, o))
            this.orders = demo
            uni.showToast({ title: 'Demo orders ready', icon: 'success' })
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        goDetail(id) { uni.navigateTo({ url: '/pages/order/detail/index?id=' + id }) }
    }
}
</script>

<style scoped>
.page { background: #F5F5F5; min-height: 100vh; }
.tabs { display: flex; background: #fff; position: sticky; top: 0; z-index: 10; border-bottom: 1rpx solid #eee; }
.tab { flex: 1; text-align: center; padding: 28rpx 0; font-size: 28rpx; color: #666; }
.tab.active { color: #0f62fe; border-bottom: 4rpx solid #0f62fe; font-weight: bold; }
.demo-bar { padding: 24rpx; }
.demo-btn { background: linear-gradient(90deg, #f53f2c, #ff7d00); color: #fff; text-align: center; padding: 24rpx; border-radius: 16rpx; font-size: 30rpx; }
.order-list { padding-bottom: 40rpx; }
.order-item { margin: 16rpx 20rpx; background: #fff; border-radius: 12rpx; padding: 20rpx; }
.order-header { display: flex; justify-content: space-between; font-size: 26rpx; color: #666; margin-bottom: 12rpx; }
.status { font-size: 24rpx; padding: 4rpx 12rpx; border-radius: 8rpx; }
.s0 { color: #ff7d00; background: #fff7e6; }
.s1 { color: #0f62fe; background: #e6f7ff; }
.s2 { color: #722ed1; background: #f9f0ff; }
.s3 { color: #52c41a; background: #f6ffed; }
.s4 { color: #999; background: #f5f5f5; }
.s5 { color: #999; background: #f5f5f5; }
.s6 { color: #fadb14; background: #feffe6; }
.s7 { color: #999; background: #f5f5f5; }
.order-body { display: flex; align-items: center; gap: 16rpx; padding: 12rpx 0; border-top: 1rpx solid #f5f5f5; }
.g-thumb { width: 100rpx; height: 100rpx; border-radius: 12rpx; background: #f5f5f5; }
.g-text { flex: 1; display: flex; flex-direction: column; }
.g-name { font-size: 28rpx; color: #333; }
.g-qty { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.g-price { font-size: 28rpx; color: #f53f2c; font-weight: bold; }
.order-footer { text-align: right; font-size: 28rpx; padding-top: 12rpx; border-top: 1rpx solid #f5f5f5; }
.amount { color: #f53f2c; font-weight: bold; }
.shipping-tip { margin-top: 16rpx; padding: 12rpx 16rpx; background: #f0f6ff; border-radius: 8rpx; font-size: 24rpx; color: #0f62fe; }
.empty, .empty-tip { text-align: center; color: #999; padding: 80rpx 0; }
</style>
