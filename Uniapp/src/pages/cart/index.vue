<template>
    <view class="page">
        <view v-if="items.length > 0">
            <view class="cart-list">
                <view class="cart-item" v-for="item in items" :key="item.id">
                    <view class="check" @click="toggleCheck(item)">{{ item.checked ? '✓' : '○' }}</view>
                    <image :src="item.imageUrl || '/static/logo.png'" mode="aspectFill" class="cart-img" />
                    <view class="cart-info">
                        <text class="cart-name">{{ item.goodsName }}</text>
                        <text class="cart-spec" v-if="item.spec">{{ item.spec }}</text>
                        <view class="cart-bottom">
                            <text class="cart-price">{{ $t('common.yuan') }}{{ formatPrice(item.price) }}</text>
                            <view class="qty-ctrl">
                                <text class="qty-btn" @click="changeQty(item, -1)">-</text>
                                <text class="qty-num">{{ item.quantity }}</text>
                                <text class="qty-btn" @click="changeQty(item, 1)">+</text>
                            </view>
                        </view>
                    </view>
                    <text class="del" @click="del(item)">🗑</text>
                </view>
            </view>
            <view class="bottom-bar">
                <view class="check" @click="toggleAll">{{ allChecked ? '✓' : '○' }}</view>
                <text class="select-all">{{ $t('cart.selected') }}</text>
                <view class="total-row">
                    <text>{{ $t('cart.total') }}: </text>
                    <text class="total-num">{{ $t('common.yuan') }}{{ formatPrice(totalPrice) }}</text>
                </view>
                <view class="btn-settle" @click="goConfirm">{{ $t('cart.settle') }} ({{ checkedItems.length }})</view>
            </view>
        </view>
        <view v-else class="empty">
            <text class="empty-icon">🛒</text>
            <text class="empty-text">{{ $t('cart.empty') }}</text>
            <view class="go-shop" @click="goShop">{{ $t('home.allGoods') }}</view>
        </view>
    </view>
</template>

<script>
import { cartApi } from '@/api/index.js'
import { useCartStore } from '@/stores/index.js'

function parseSpec(specJson) {
    if (!specJson) return ''
    try {
        const obj = typeof specJson === 'string' ? JSON.parse(specJson) : specJson
        if (Array.isArray(obj)) return obj.map(x => x.value || x.name).filter(Boolean).join(' / ')
        if (obj && typeof obj === 'object') return Object.values(obj).filter(Boolean).join(' / ')
    } catch (e) {}
    return ''
}

export default {
    data() { return { items: [], loading: false } },
    computed: {
        cart() { return useCartStore() },
        allChecked() { return this.items.length > 0 && this.items.every(i => i.checked) },
        totalPrice() { return this.items.filter(i => i.checked).reduce((s, i) => s + i.price * i.quantity, 0) },
        checkedItems() { return this.items.filter(i => i.checked) }
    },
    onShow() { this.loadCart() },
    methods: {
        async loadCart() {
            this.loading = true
            try {
                const res = await cartApi.list()
                const list = (res?.list || res || [])
                if (Array.isArray(list) && list.length) {
                    this.items = list.map(item => ({
                        id: item.id,
                        spuId: item.spuId,
                        skuId: item.skuId,
                        goodsName: item.spuName || item.goodsName || '',
                        price: Number(item.price || 0),
                        quantity: item.quantity || 1,
                        imageUrl: item.imageUrl || '/static/logo.png',
                        spec: parseSpec(item.specJson),
                        checked: item.selected === 1 || item.selected === true || item.checked === true,
                        stock: item.stock || 0
                    }))
                    return
                }
            } catch (e) {
                // 走离线演示数据兜底
            }
            const local = this.cart.state.items
            this.items = local.map(i => ({ ...i, checked: i.checked !== false }))
            this.loading = false
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        toggleCheck(item) { item.checked = !item.checked; this.cart.save() },
        toggleAll() {
            const target = !this.allChecked
            this.items.forEach(i => i.checked = target)
            this.cart.save()
        },
        async changeQty(item, delta) {
            const newQty = Math.max(1, item.quantity + delta)
            if (newQty === item.quantity) return
            item.quantity = newQty
            try {
                await cartApi.update({ id: item.id, quantity: newQty })
            } catch (e) {}
            this.cart.updateQty(item.id, newQty)
        },
        async del(item) {
            uni.showModal({
                title: this.$t('common.confirm'),
                content: this.$t('cart.removed'),
                success: async r => {
                    if (!r.confirm) return
                    const idx = this.items.findIndex(i => i.id === item.id)
                    if (idx >= 0) this.items.splice(idx, 1)
                    try {
                        await cartApi.remove(item.id)
                    } catch (e) {}
                    this.cart.remove(item.id)
                }
            })
        },
        goConfirm() {
            const checked = this.checkedItems
            if (!checked.length) return uni.showToast({ title: 'cart empty', icon: 'none' })
            uni.navigateTo({ url: '/pages/order/confirm/index' })
        },
        goShop() { uni.switchTab({ url: '/pages/goods/list/index' }) }
    }
}
</script>

<style scoped>
.page { padding-bottom: 140rpx; }
.cart-item { display: flex; align-items: center; padding: 20rpx; background: #fff; margin: 10rpx 20rpx; border-radius: 12rpx; }
.check { width: 48rpx; height: 48rpx; line-height: 48rpx; text-align: center; font-size: 32rpx; color: #0f62fe; }
.cart-img { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #f5f5f5; margin: 0 16rpx; }
.cart-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.cart-name { font-size: 28rpx; color: #333; }
.cart-spec { font-size: 24rpx; color: #999; }
.cart-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8rpx; }
.cart-price { font-size: 32rpx; color: #f53f2c; font-weight: bold; }
.qty-ctrl { display: flex; align-items: center; gap: 16rpx; }
.qty-btn { width: 48rpx; height: 48rpx; line-height: 48rpx; text-align: center; background: #f5f5f5; border-radius: 8rpx; font-size: 28rpx; }
.qty-num { min-width: 40rpx; text-align: center; font-size: 28rpx; }
.del { font-size: 32rpx; padding: 0 12rpx; }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; align-items: center; gap: 12rpx; padding: 16rpx 24rpx; background: #fff; box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04); }
.select-all { font-size: 26rpx; color: #555; }
.total-row { flex: 1; font-size: 26rpx; color: #555; text-align: right; }
.total-num { font-size: 32rpx; color: #f53f2c; font-weight: bold; }
.btn-settle { height: 72rpx; line-height: 72rpx; padding: 0 32rpx; background: linear-gradient(90deg, #f53f2c, #ff7d00); color: #fff; border-radius: 36rpx; font-size: 28rpx; }
.empty { display: flex; flex-direction: column; align-items: center; padding: 200rpx 0 0; }
.empty-icon { font-size: 120rpx; }
.empty-text { font-size: 28rpx; color: #999; margin-top: 24rpx; }
.go-shop { margin-top: 40rpx; padding: 20rpx 48rpx; border-radius: 40rpx; background: #0f62fe; color: #fff; font-size: 28rpx; }
</style>
