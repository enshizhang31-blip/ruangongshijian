<template>
    <view class="page">
        <view class="address" @click="selectAddress">
            <text v-if="address" class="addr-name">{{ address.receiverName || address.name || '-' }}  {{ address.phone }}</text>
            <text v-else class="addr-empty">📍 {{ $t('order.address') }} ></text>
            <text v-if="address" class="addr-detail">{{ address.province || '' }}{{ address.city || '' }}{{ address.detail || '' }}</text>
        </view>

        <view class="goods-list">
            <view class="goods-item" v-for="(g, idx) in goods" :key="idx">
                <image class="g-img" :src="g.imageUrl || '/static/logo.png'" mode="aspectFill" />
                <view class="g-info">
                    <text class="g-name">{{ g.goodsName || g.name }}</text>
                    <text class="g-spec" v-if="g.spec">{{ g.spec }}</text>
                    <view class="g-bottom">
                        <text class="g-price">{{ $t('common.yuan') }}{{ formatPrice(g.price) }}</text>
                        <text class="g-qty">×{{ g.quantity }}</text>
                    </view>
                </view>
            </view>
        </view>

        <view class="total">
            <text>{{ $t('cart.total') }}: </text>
            <text class="total-num">{{ $t('common.yuan') }}{{ formatPrice(totalPrice) }}</text>
        </view>

        <view style="height: 160rpx;" />
        <button class="btn-pay" @click="submit" :loading="submitting" :disabled="submitting">
            {{ $t('order.pay') }}
        </button>
    </view>
</template>

<script>
import { addressApi, orderApi } from '@/api/index.js'
import { useCartStore } from '@/stores/index.js'

export default {
    data() { return { address: null, goods: [], submitting: false } },
    computed: {
        cart() { return useCartStore() },
        totalPrice() { return this.goods.reduce((s, g) => s + (g.price || 0) * g.quantity, 0) }
    },
    onShow() { this.loadCartItems() },
    onLoad() { this.loadAddress() },
    methods: {
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        loadCartItems() {
            const items = this.cart.checkedItems()
            this.goods = items.length ? items : this.cart.state.items
        },
        async loadAddress() {
            try {
                const res = await addressApi.list().catch(() => null)
                const list = res?.list || res || []
                this.address = list.find(a => a.isDefault) || list[0] || null
            } catch (e) {
                this.address = { receiverName: '演示收件人', phone: '13800000000', province: '广东省', city: '深圳市', detail: '南山区演示街 1 号 101' }
            }
        },
        selectAddress() { uni.navigateTo({ url: '/pages/address/list/index' }) },
        async submit() {
            if (!this.goods.length) { uni.showToast({ title: this.$t('cart.empty'), icon: 'none' }); return }
            this.submitting = true
            try {
                if (this.address) {
                    await orderApi.create({ addressId: this.address.id, items: this.goods.map(g => ({ skuId: g.skuId, spuId: g.spuId, quantity: g.quantity })) }).catch(() => null)
                }
                const orderId = Date.now()
                const order = {
                    id: orderId, orderNo: 'D' + orderId, status: 2, totalAmount: this.totalPrice,
                    createdAt: this.formatTime(new Date()),
                    receiverName: this.address?.receiverName || '-', receiverPhone: this.address?.phone || '',
                    address: this.address ? `${this.address.province || ''}${this.address.city || ''}${this.address.detail || ''}` : '-',
                    items: this.goods.map(g => ({ id: g.id, spuId: g.spuId, skuId: g.skuId, goodsName: g.goodsName || g.name, imageUrl: g.imageUrl, quantity: g.quantity, price: g.price, spec: g.spec }))
                }
                uni.setStorageSync(`demo-order-${orderId}`, order)
                const list = uni.getStorageSync('demo-orders') || []
                list.unshift(order)
                uni.setStorageSync('demo-orders', list)
                this.cart.clear()
                uni.showToast({ title: this.$t('common.success'), icon: 'success' })
                setTimeout(() => uni.redirectTo({ url: '/pages/order/detail/index?id=' + orderId }), 600)
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('toast.opFailed'), icon: 'none' })
            } finally { this.submitting = false }
        },
        formatTime(d) {
            const p = n => String(n).padStart(2, '0')
            return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
        }
    }
}
</script>

<style scoped>
.page { padding: 20rpx; padding-bottom: 160rpx; background: #F5F5F5; min-height: 100vh; }
.address { padding: 24rpx; background: #fff; border-radius: 12rpx; margin-bottom: 16rpx; display: flex; flex-direction: column; gap: 6rpx; }
.addr-name { font-size: 30rpx; font-weight: bold; color: #333; }
.addr-empty { font-size: 28rpx; color: #f53f2c; }
.addr-detail { font-size: 24rpx; color: #666; }
.goods-list { background: #fff; border-radius: 12rpx; overflow: hidden; margin-bottom: 16rpx; }
.goods-item { display: flex; gap: 16rpx; padding: 16rpx; border-bottom: 1rpx solid #f5f5f5; }
.goods-item:last-child { border-bottom: none; }
.g-img { width: 120rpx; height: 120rpx; border-radius: 8rpx; background: #f5f5f5; }
.g-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.g-name { font-size: 28rpx; color: #333; }
.g-spec { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.g-bottom { display: flex; justify-content: space-between; margin-top: 4rpx; }
.g-price { font-size: 28rpx; color: #f53f2c; font-weight: bold; }
.g-qty { font-size: 26rpx; color: #999; }
.total { text-align: right; padding: 24rpx; background: #fff; border-radius: 12rpx; font-size: 28rpx; color: #555; }
.total-num { font-size: 36rpx; color: #f53f2c; font-weight: bold; margin-left: 8rpx; }
.btn-pay { position: fixed; bottom: 20rpx; left: 20rpx; right: 20rpx; height: 88rpx; line-height: 88rpx; background: linear-gradient(90deg, #f53f2c, #ff7d00); color: #fff; border-radius: 44rpx; font-size: 32rpx; border: none; }
</style>
