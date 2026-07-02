<template>
    <view class="page" v-if="order">
        <view class="status-bar" :class="statusClass">
            <text class="status-icon">{{ statusIcon }}</text>
            <view class="status-text">
                <text class="status-title">{{ statusTitle }}</text>
                <text class="status-desc" v-if="order.status === 2">{{ $t('logistics.transit') }} · {{ etaTime }}</text>
            </view>
        </view>

        <view class="card" v-if="order.status >= 2 && order.status < 5 && order.status !== 6">
            <view class="card-title">{{ $t('order.logistics') }}</view>
            <view class="timeline">
                <view class="timeline-item" v-for="(node, idx) in logisticsList" :key="idx"
                      :class="{ active: idx === 0, passed: idx > 0 }">
                    <view class="dot"></view>
                    <view class="timeline-content">
                        <text class="t-title">{{ node.title }}</text>
                        <text class="t-time">{{ node.time }}</text>
                    </view>
                </view>
                <view class="empty-tip" v-if="!logisticsList.length">{{ $t('order.logisticsEmpty') }}</view>
            </view>
        </view>

        <view class="card address-card">
            <text class="addr-name">{{ order.receiverName || '-' }}  {{ order.receiverPhone || '' }}</text>
            <text class="addr-detail">{{ order.address || '-' }}</text>
        </view>

        <view class="card">
            <view class="card-title">{{ $t('order.orderNo') }}:{{ order.orderNo || order.id }}</view>
            <view class="goods-row" v-for="i in (order.items || [])" :key="i.id || i.spuId">
                <image class="g-img" :src="i.imageUrl || '/static/logo.png'" mode="aspectFill" />
                <view class="g-info">
                    <text class="g-name">{{ i.goodsName || i.spuName }}</text>
                    <text class="g-spec">{{ i.spec || '' }}</text>
                    <view class="g-bottom">
                        <text class="g-price">{{ $t('common.yuan') }}{{ formatPrice(i.price) }}</text>
                        <text class="g-qty">×{{ i.quantity }}</text>
                    </view>
                </view>
            </view>
        </view>

        <view class="card">
            <view class="row">
                <text>{{ $t('order.totalAmount') }}</text>
                <text class="amount">{{ $t('common.yuan') }}{{ formatPrice(order.totalAmount) }}</text>
            </view>
            <view class="row">
                <text>{{ $t('order.placedAt') }}</text>
                <text class="muted">{{ order.createdAt || '-' }}</text>
            </view>
        </view>

        <view style="height: 200rpx;" />

        <view class="footer">
            <view v-if="order.status === 0" class="op-btn primary" @click="onPay">
                <text>{{ $t('order.pay') }}</text>
            </view>
            <view v-if="order.status === 0" class="op-btn ghost" @click="onCancel">
                <text>{{ $t('order.cancel') }}</text>
            </view>
            <view v-if="order.status === 1" class="op-btn primary" @click="onShip">
                <text>{{ $t('order.ship') || '模拟发货' }}</text>
            </view>
            <view v-if="order.status === 2" class="op-btn primary" @click="onReceive">
                <text>{{ $t('order.receive') }}</text>
            </view>
            <view v-if="order.status === 1 || order.status === 2 || order.status === 3" class="op-btn ghost" @click="onRefund">
                <text>{{ $t('order.refund') }}</text>
            </view>
            <view v-if="order.status === 6" class="op-btn primary" @click="onRefundComplete">
                <text>{{ $t('order.refundComplete') || '退款完成' }}</text>
            </view>
        </view>
    </view>
    <view v-else class="loading-page">
        <text>{{ $t('common.loading') }}</text>
    </view>
</template>

<script>
import { orderApi } from '@/api/index.js'

const STATUS_ICON = { 0: '⏳', 1: '💳', 2: '🚚', 3: '📦', 4: '✅', 5: '✖', 6: '↩', 7: '💰' }

export default {
    data() { return { orderId: null, order: null, logisticsList: [] } },
    computed: {
        statusText() { return this.$t('status.' + (this.order?.status ?? 0)) },
        statusTitle() { return this.statusText },
        statusIcon() { return STATUS_ICON[this.order?.status] || '📋' },
        statusClass() {
            const s = this.order?.status
            if (s === 0) return 's-pending'
            if (s === 2) return 's-shipping'
            if (s === 3 || s === 4) return 's-done'
            return 's-default'
        },
        etaTime() {
            const d = new Date(); d.setHours(18, 0, 0, 0)
            const p = n => String(n).padStart(2, '0')
            return `${d.getMonth() + 1}-${p(d.getDate())} 18:00`
        }
    },
    onLoad(options) {
        this.orderId = options.id
        const cached = uni.getStorageSync(`demo-order-${options.id}`)
        if (cached && cached.items && cached.items.length) {
            // 仅在缓存与后端字段都齐全时短暂展示，立即用后端数据覆盖
            this.order = cached
        }
        this.loadFromBackend(options.id)
    },
    methods: {
        async loadFromBackend(id) {
            try {
                const data = await orderApi.detail(id)
                this.order = data
                this.buildLogistics(data)
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('toast.networkError'), icon: 'none' })
            }
        },
        buildLogistics(order) {
            const status = order.status
            if (status < 2) { this.logisticsList = []; return }
            const now = new Date()
            const fmt = d => `${d.getMonth() + 1}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
            const back = h => new Date(now.getTime() - h * 60 * 60 * 1000)
            const items = []
            if (status >= 3) items.push({ title: this.$t('logistics.signed'), time: fmt(back(0)) })
            if (status >= 2) {
                items.push({ title: this.$t('logistics.dispatch'), time: fmt(back(2)) })
                items.push({ title: this.$t('logistics.transit'), time: fmt(back(6)) })
                items.push({ title: this.$t('logistics.picked'), time: fmt(back(12)) })
            }
            this.logisticsList = items
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        async onPay() {
            try { await orderApi.pay(this.orderId, 1) }
            catch (_) {}
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 1
            } catch (_) { this.order.status = 1 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        },
        async onCancel() {
            try { await orderApi.cancel(this.orderId) }
            catch (_) {}
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 5
            } catch (_) { this.order.status = 5 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        },
        async onReceive() {
            try { await orderApi.confirmReceive(this.orderId) }
            catch (_) {}
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 3
            } catch (_) { this.order.status = 3 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        },
        async onShip() {
            try { await orderApi.ship(this.orderId) }
            catch (e) { return uni.showToast({ title: e.message || '发货失败', icon: 'none' }) }
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 2
            } catch (_) { this.order.status = 2 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        },
        async onRefundComplete() {
            try { await orderApi.refundComplete(this.orderId) }
            catch (e) { return uni.showToast({ title: e.message || '操作失败', icon: 'none' }) }
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 7
            } catch (_) { this.order.status = 7 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        },
        async onRefund() {
            try {
                await orderApi.refund(this.orderId, 'demo')
            } catch (_) {}
            try {
                const data = await orderApi.detail(this.orderId)
                if (data) { this.order = data; this.buildLogistics(data) }
                else this.order.status = 6
            } catch (_) { this.order.status = 6 }
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        }
    }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F5F5F5; padding-bottom: 20rpx; }
.status-bar { display: flex; align-items: center; padding: 48rpx 32rpx; color: #fff; font-weight: bold; }
.status-bar.s-pending { background: #ff7d00; }
.status-bar.s-shipping { background: linear-gradient(90deg, #0f62fe, #5e9eff); }
.status-bar.s-done { background: #52c41a; }
.status-bar.s-default { background: #888; }
.status-icon { font-size: 80rpx; margin-right: 24rpx; }
.status-title { font-size: 40rpx; }
.status-desc { font-size: 24rpx; opacity: 0.85; margin-top: 8rpx; }
.card { background: #fff; margin: 16rpx; padding: 24rpx; border-radius: 12rpx; }
.card-title { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
.timeline { position: relative; padding-left: 16rpx; }
.timeline-item { display: flex; gap: 24rpx; padding: 16rpx 0; position: relative; }
.timeline-item .dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #ccc; margin-top: 8rpx; flex-shrink: 0; }
.timeline-item.active .dot { background: #0f62fe; box-shadow: 0 0 0 6rpx rgba(15,98,254,0.15); }
.timeline-item.passed .dot { background: #0f62fe; opacity: 0.5; }
.t-title { font-size: 28rpx; color: #333; }
.t-time { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.empty-tip { text-align: center; color: #999; padding: 32rpx 0; }
.address-card { display: flex; flex-direction: column; gap: 8rpx; }
.addr-name { font-size: 30rpx; font-weight: bold; color: #333; }
.addr-detail { font-size: 26rpx; color: #666; }
.goods-row { display: flex; gap: 16rpx; padding: 16rpx 0; border-top: 1rpx solid #f5f5f5; }
.g-img { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #f5f5f5; }
.g-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.g-name { font-size: 28rpx; color: #333; }
.g-spec { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.g-bottom { display: flex; justify-content: space-between; margin-top: 8rpx; }
.g-price { font-size: 28rpx; color: #f53f2c; font-weight: bold; }
.g-qty { font-size: 26rpx; color: #999; }
.row { display: flex; justify-content: space-between; padding: 12rpx 0; font-size: 28rpx; }
.amount { color: #f53f2c; font-weight: bold; }
.muted { color: #999; }
.footer { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 16rpx 24rpx; display: flex; gap: 16rpx; justify-content: flex-end; box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04); }
.op-btn { min-width: 180rpx; height: 72rpx; line-height: 72rpx; text-align: center; border-radius: 36rpx; font-size: 28rpx; padding: 0 32rpx; }
.op-btn.primary { background: #0f62fe; color: #fff; }
.op-btn.ghost { background: #fff; color: #555; border: 1rpx solid #ddd; }
.loading-page { padding: 200rpx 0; text-align: center; color: #999; }
</style>
