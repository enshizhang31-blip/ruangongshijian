<template>
    <view class="page" v-if="goods">
        <swiper class="banner" :indicator-dots="true" :autoplay="true" :circular="true">
            <swiper-item v-for="(img, idx) in images" :key="idx">
                <image :src="img" mode="aspectFill" class="banner-img" />
            </swiper-item>
        </swiper>

        <view class="info-card">
            <view class="price-row">
                <text class="price-symbol">{{ $t('common.yuan') }}</text>
                <text class="price-num">{{ formatPrice(goodsPrice()) }}</text>
            </view>
            <view class="goods-name">{{ goods.name }}</view>
            <view class="meta-row">
                <text class="meta">{{ $t('goods.sales') }} {{ goods.salesCount || 0 }}</text>
                <text class="meta">{{ $t('goods.stock') }} {{ goods.stock || 0 }}</text>
            </view>
        </view>

        <view class="card qty-card">
            <text class="card-label">{{ $t('goods.qty') }}</text>
            <view class="qty-ctrl">
                <text class="qty-btn" @click="decQty">-</text>
                <text class="qty-num">{{ quantity }}</text>
                <text class="qty-btn" @click="incQty">+</text>
            </view>
        </view>

        <view class="card">
            <view class="card-title">{{ $t('goods.desc') }}</view>
            <view class="desc-body">
                <text v-if="goods.description">{{ goods.description }}</text>
                <text v-else class="empty">{{ $t('goods.noDesc') }}</text>
            </view>
        </view>

        <view style="height: 160rpx;" />

        <view class="footer">
            <view class="btn btn-cart" @click="addToCart">
                <text class="btn-icon">🛒</text>
                <text>{{ $t('goods.addToCart') }}</text>
            </view>
            <view class="btn btn-buy" @click="buyNow">
                <text>{{ $t('goods.buyNow') }}</text>
            </view>
        </view>
    </view>
    <view v-else class="loading-page">
        <text>{{ $t('common.loading') }}</text>
    </view>
</template>

<script>
import { spuApi, cartApi } from '@/api/index.js'
import { useUserStore, useCartStore } from '@/stores/index.js'

export default {
    data() { return { goods: null, quantity: 1, images: [] } },
    onLoad(options) {
        if (options.id) this.loadDetail(options.id)
        else uni.showToast({ title: 'missing id', icon: 'none' })
    },
    methods: {
        async loadDetail(id) {
            try {
                const detail = await spuApi.detail(id)
                this.goods = detail || {}
                this.images = []
                if (this.goods.imageUrl) this.images.push(this.goods.imageUrl)
                if (this.goods.mainImage) this.images.push(this.goods.mainImage)
                if (this.goods.images) {
                    if (typeof this.goods.images === 'string') {
                        this.goods.images.split(',').map(s => s.trim()).filter(Boolean).forEach(x => this.images.push(x))
                    } else if (Array.isArray(this.goods.images)) {
                        this.images.push(...this.goods.images)
                    }
                }
                if (!this.images.length) this.images = ['/static/logo.png']
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('toast.networkError'), icon: 'none' })
            }
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        goodsPrice() {
            const skus = this.goods && this.goods.skus
            if (Array.isArray(skus) && skus.length) return Math.min(...skus.map(s => Number(s.price || 0)))
            return Number((this.goods && this.goods.price) || 0)
        },
        incQty() { this.quantity++ },
        decQty() { if (this.quantity > 1) this.quantity-- },
        ensureLogin() {
            const user = useUserStore()
            if (!user.isLoggedIn.value) { uni.navigateTo({ url: '/pages/auth/login/index' }); return false }
            return true
        },
        firstSku() {
            const skus = this.goods && this.goods.skus
            return Array.isArray(skus) && skus.length ? skus[0] : null
        },
        async addToCart() {
            if (!this.ensureLogin()) return
            const sku = this.firstSku()
            if (!sku) {
                uni.showToast({ title: '该商品暂无可售 SKU', icon: 'none' })
                return
            }
            if ((sku.stock || 0) <= 0) {
                uni.showToast({ title: '库存不足', icon: 'none' })
                return
            }
            const cart = useCartStore()
            cart.addItem({
                spuId: this.goods.id, skuId: sku.id,
                name: this.goods.name, price: Number(sku.price || 0),
                quantity: this.quantity, imageUrl: sku.imageUrl || (this.images && this.images[0]) || ''
            })
            try {
                await cartApi.add({ spuId: this.goods.id, skuId: sku.id, quantity: this.quantity })
                uni.showToast({ title: this.$t('toast.addedToCart'), icon: 'success' })
            } catch (e) {
                uni.showToast({ title: '已加入本地购物车', icon: 'none' })
            }
        },
        async buyNow() {
            if (!this.ensureLogin()) return
            const sku = this.firstSku()
            if (!sku) {
                uni.showToast({ title: '该商品暂无可售 SKU', icon: 'none' })
                return
            }
            if ((sku.stock || 0) <= 0) {
                uni.showToast({ title: '库存不足', icon: 'none' })
                return
            }
            const cart = useCartStore()
            // 清理旧临时项
            cart.clear()
            cart.addItem({
                spuId: this.goods.id, skuId: sku.id,
                name: this.goods.name, price: Number(sku.price || 0),
                quantity: this.quantity, imageUrl: sku.imageUrl || (this.images && this.images[0]) || '',
                spec: sku.specJson || '', checked: true
            })
            try { await cartApi.add({ spuId: this.goods.id, skuId: sku.id, quantity: this.quantity }) } catch (_) {}
            uni.navigateTo({ url: '/pages/order/confirm/index' })
        }
    }
}
</script>

<style scoped>
.page { padding-bottom: 20rpx; background: #F5F5F5; min-height: 100vh; }
.banner { width: 100%; height: 600rpx; background: #fff; }
.banner-img { width: 100%; height: 600rpx; }
.info-card { background: #fff; padding: 32rpx 24rpx; margin-bottom: 16rpx; }
.price-row { display: flex; align-items: baseline; color: #f53f2c; }
.price-symbol { font-size: 28rpx; }
.price-num { font-size: 56rpx; font-weight: bold; }
.goods-name { font-size: 32rpx; color: #333; margin-top: 16rpx; line-height: 1.4; }
.meta-row { display: flex; gap: 32rpx; margin-top: 16rpx; }
.meta { font-size: 24rpx; color: #999; }
.card { background: #fff; margin: 16rpx 0; padding: 24rpx; }
.card-label, .card-title { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
.qty-card { display: flex; justify-content: space-between; align-items: center; }
.qty-ctrl { display: flex; align-items: center; gap: 24rpx; }
.qty-btn { width: 60rpx; height: 60rpx; line-height: 60rpx; text-align: center; background: #f5f5f5; border-radius: 12rpx; font-size: 32rpx; }
.qty-num { min-width: 60rpx; text-align: center; font-size: 30rpx; }
.desc-body { font-size: 28rpx; color: #555; line-height: 1.6; }
.empty { color: #999; }
.footer { position: fixed; bottom: 0; left: 0; right: 0; display: flex; background: #fff; border-top: 1rpx solid #eee; box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04); }
.btn { flex: 1; height: 96rpx; display: flex; align-items: center; justify-content: center; gap: 8rpx; font-size: 28rpx; }
.btn-icon { font-size: 32rpx; }
.btn-cart { color: #333; background: #fff; border-right: 1rpx solid #eee; }
.btn-buy { color: #fff; background: linear-gradient(90deg, #f53f2c, #ff7d00); }
.loading-page { padding: 200rpx 0; text-align: center; color: #999; }
</style>
