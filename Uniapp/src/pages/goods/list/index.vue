<template>
    <view class="page">
        <view class="search-bar">
            <input class="search-input" v-model="keyword" placeholder="搜索商品..." @confirm="onSearch" />
        </view>
        <view class="goods-list">
            <view class="goods-item" v-for="g in goods" :key="g.id" @click="goDetail(g.id)">
                <image :src="g.imageUrl || '/static/placeholder.png'" mode="aspectFill" class="goods-img" />
                <view class="goods-info">
                    <text class="goods-name">{{ g.name }}</text>
                    <text class="goods-price">¥{{ formatPrice(g.price || g.minPrice) }}</text>
                </view>
            </view>
        </view>
        <view v-if="goods.length === 0" class="empty">暂无数据</view>
    </view>
</template>

<script>
import { spuApi } from '@/api/index.js'

export default {
    data() {
        return {
            keyword: '',
            goods: [],
            categoryId: null
        }
    },
    onLoad(options) {
        if (options.categoryId) this.categoryId = options.categoryId
        this.loadGoods()
    },
    onShow() {
        // 从首页/我的页通过 switchTab 跳过来时，categoryId 存在 storage
        try {
            const f = uni.getStorageSync('__goodsListFilter')
            if (f && f.ts && (Date.now() - f.ts < 3000)) {
                this.categoryId = f.categoryId || null
                uni.removeStorageSync('__goodsListFilter')
                this.loadGoods()
            }
        } catch (_) {}
    },
    methods: {
        async loadGoods() {
            try {
                const res = await spuApi.list({
                    keyword: this.keyword || undefined,
                    categoryId: this.categoryId || undefined,
                    page: 1,
                    pageSize: 20
                })
                const list = (res?.list || res || [])
                this.goods = list.map(g => ({
                    ...g,
                    price: g.price || g.minPrice || 0
                }))
            } catch (e) {
                this.goods = []
            }
        },
        onSearch() {
            this.loadGoods()
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        goDetail(goodsId) {
            uni.navigateTo({ url: `/pages/goods/detail/index?id=${goodsId}` })
        }
    }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.search-bar { margin-bottom: 20rpx; }
.search-input { height: 72rpx; background: #fff; border-radius: 36rpx; padding: 0 32rpx; font-size: 28rpx; }
.goods-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.goods-item { width: calc(50% - 8rpx); background: #fff; border-radius: 12rpx; overflow: hidden; }
.goods-img { width: 100%; height: 200rpx; background: #e5e5e5; }
.goods-info { padding: 12rpx; }
.goods-name { font-size: 28rpx; color: #333; }
.goods-price { font-size: 32rpx; color: #f53f2c; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
