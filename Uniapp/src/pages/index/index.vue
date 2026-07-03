<template>
    <view class="page">
        <view class="banner">
            <swiper :indicator-dots="true" :autoplay="true" class="swiper">
                <swiper-item>
                    <view class="swiper-item gradient-1">
                        <text class="banner-title">SaleManager</text>
                        <text class="banner-sub">{{ $t('home.bannerTip') }}</text>
                    </view>
                </swiper-item>
                <swiper-item>
                    <view class="swiper-item gradient-2">
                        <text class="banner-title">{{ $t('home.title') }}</text>
                        <text class="banner-sub">i18n · member · cart · order</text>
                    </view>
                </swiper-item>
                <swiper-item>
                    <view class="swiper-item gradient-3">
                        <text class="banner-title">Vue 3 · uni-app</text>
                        <text class="banner-sub">{{ $t('member.demoAccount') }}</text>
                    </view>
                </swiper-item>
            </swiper>
        </view>

        <view class="section">
            <view class="section-title">{{ $t('home.quickEntries') }}</view>
            <view class="quick-grid">
                <view class="quick-item" @click="goScan">
                    <view class="quick-icon scan">📷</view>
                    <text class="quick-name">{{ $t('home.scanCode') }}</text>
                </view>
                <view class="quick-item" @click="goGoodsList()">
                    <view class="quick-icon list">📋</view>
                    <text class="quick-name">{{ $t('home.allGoods') }}</text>
                </view>
                <view class="quick-item" @click="goOrders">
                    <view class="quick-icon order">📦</view>
                    <text class="quick-name">{{ $t('member.orders') }}</text>
                </view>
            </view>
        </view>

        <view class="section">
            <view class="section-title">{{ $t('home.categories') }}</view>
            <view class="category-grid">
                <view class="category-item" v-for="c in categories" :key="c.id" @click="goGoodsList(c.id)">
                    <text class="category-name">{{ c.name }}</text>
                </view>
                <view class="category-item empty" v-if="!categories.length">{{ $t('home.categoryFallback') }}</view>
            </view>
        </view>

        <view class="section">
            <view class="section-title">{{ $t('home.recommend') }}</view>
            <view class="goods-grid">
                <view class="goods-item" v-for="g in goods" :key="g.id" @click="goDetail(g.id)">
                    <image :src="g.imageUrl || g.mainImage || '/static/logo.png'" mode="aspectFill" class="goods-img" />
                    <view class="goods-info">
                        <text class="goods-name">{{ g.name }}</text>
                        <text class="goods-price">{{ $t('common.yuan') }}{{ formatPrice(g.price) }}</text>
                    </view>
                </view>
                <view class="empty-cell" v-if="!goods.length">{{ $t('home.goodsFallback') }}</view>
            </view>
        </view>
    </view>
</template>

<script>
import { categoryApi, spuApi } from '@/api/index.js'
import { useUserStore } from '@/stores/index.js'

export default {
    data() { return { categories: [], goods: [], loading: true, __requireLogin: false } },
    computed: { user() { return useUserStore() } },
    onShow() { this.loadData() },
    methods: {
        async loadData() {
            this.loading = true
            try {
                const [categories, goods] = await Promise.all([
                    categoryApi.list().catch(() => []),
                    spuApi.list({ page: 1, pageSize: 10 }).catch(() => null)
                ])
                this.categories = Array.isArray(categories) ? categories : (categories?.list || [])
                const list = goods?.list || goods || []
                this.goods = Array.isArray(list) ? list.map(g => ({
                    ...g,
                    price: g.price || g.minPrice || 0
                })) : []
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('toast.networkError'), icon: 'none' })
            } finally { this.loading = false }
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        goGoodsList(id) {
            // goods/list 是 tabBar 页面，必须用 switchTab（switchTab 不支持 query 传参，用 storage 中转）
            try { uni.setStorageSync('__goodsListFilter', { categoryId: id || null, ts: Date.now() }) } catch (_) {}
            uni.switchTab({ url: '/pages/goods/list/index' })
        },
        goDetail(id) { uni.navigateTo({ url: '/pages/goods/detail/index?id=' + id }) },
        goScan() { uni.switchTab({ url: '/pages/goods/list/index' }) },
        goOrders() {
            if (this.user.requireLogin()) uni.navigateTo({ url: '/pages/order/list/index' })
        }
    }
}
</script>

<style scoped>
.page { padding-bottom: 20rpx; }
.banner { height: 360rpx; }
.swiper { height: 360rpx; }
.swiper-item { height: 360rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #fff; }
.gradient-1 { background: linear-gradient(135deg, #0f62fe, #5e9eff); }
.gradient-2 { background: linear-gradient(135deg, #f53f2c, #ff7d00); }
.gradient-3 { background: linear-gradient(135deg, #722ed1, #b37feb); }
.banner-title { font-size: 48rpx; font-weight: bold; }
.banner-sub { font-size: 26rpx; opacity: 0.9; margin-top: 12rpx; }
.section { margin: 20rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; }
.section-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.quick-grid { display: flex; gap: 16rpx; }
.quick-item { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 24rpx; background: #fafafa; border-radius: 16rpx; }
.quick-icon { width: 80rpx; height: 80rpx; border-radius: 40rpx; display: flex; align-items: center; justify-content: center; font-size: 40rpx; margin-bottom: 12rpx; }
.quick-icon.scan { background: #e6f7ff; }
.quick-icon.list { background: #f6ffed; }
.quick-icon.order { background: #fff7e6; }
.quick-name { font-size: 24rpx; color: #333; }
.category-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.category-item { padding: 16rpx 28rpx; background: #f0f6ff; border-radius: 12rpx; font-size: 26rpx; color: #0f62fe; }
.category-item.empty { background: #f9f9f9; color: #999; }
.goods-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.goods-item { width: calc(50% - 8rpx); border-radius: 12rpx; overflow: hidden; background: #fafafa; }
.goods-img { width: 100%; height: 200rpx; background: #f5f5f5; }
.goods-info { padding: 12rpx; }
.goods-name { font-size: 26rpx; color: #333; }
.goods-price { font-size: 32rpx; color: #f53f2c; font-weight: bold; }
.empty-cell { width: 100%; text-align: center; padding: 60rpx 0; color: #999; }
</style>
