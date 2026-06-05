<template>
    <view class="page">
        <view class="goods-detail" v-if="info">
            <image :src="info.imageUrl || '/static/placeholder.png'" mode="aspectFill" class="detail-img" />
            <view class="detail-info">
                <text class="detail-name">{{ info.name }}</text>
                <text class="detail-price">¥{{ info.price }}</text>
                <text class="detail-desc">{{ info.description || '-' }}</text>
            </view>
            <view class="spec-section">
                <text class="section-label">规格选择</text>
                <view class="spec-tags">
                    <view class="spec-tag" v-for="s in specs" :key="s.id" :class="{ active: s.selected }"
                        @click="selectSpec(s)">
                        {{ s.name }}: {{ s.value }}
                    </view>
                </view>
            </view>
        </view>
        <view class="bottom-bar">
            <button class="btn-cart" @click="addToCart">加入购物车</button>
            <button class="btn-buy" @click="buyNow">立即购买</button>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            info: null,
            specs: [],
            goodsId: null
        }
    },
    onLoad(options) {
        if (options.id) this.goodsId = options.id
        this.loadDetail()
    },
    methods: {
        async loadDetail() {
            // TODO: 调用 API 加载商品详情
        },
        selectSpec(spec) {
            spec.selected = !spec.selected
        },
        addToCart() {
            // TODO: 加入购物车
            uni.showToast({ title: '已加入购物车', icon: 'success' })
        },
        buyNow() {
            // TODO: 立即购买
            uni.navigateTo({ url: '/pages/order/confirm' })
        }
    }
}
</script>

<style scoped>
.page {
    padding-bottom: 100rpx;
}

.detail-img {
    width: 100%;
    height: 500rpx;
    background: #e5e5e5;
}

.detail-info {
    padding: 24rpx;
    background: #fff;
}

.detail-name {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
}

.detail-price {
    font-size: 40rpx;
    color: #f53f2c;
    font-weight: bold;
    margin-top: 12rpx;
    display: block;
}

.detail-desc {
    font-size: 28rpx;
    color: #666;
    margin-top: 16rpx;
}

.spec-section {
    padding: 24rpx;
    background: #fff;
    margin-top: 16rpx;
}

.section-label {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 12rpx;
}

.spec-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
}

.spec-tag {
    padding: 12rpx 24rpx;
    background: #f5f5f5;
    border-radius: 8rpx;
    font-size: 26rpx;
}

.spec-tag.active {
    background: #0f62fe;
    color: #fff;
}

.bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    padding: 16rpx 24rpx;
    background: #fff;
    gap: 16rpx;
}

.btn-cart {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    background: #fff;
    border: 2rpx solid #0f62fe;
    color: #0f62fe;
    border-radius: 40rpx;
    font-size: 28rpx;
}

.btn-buy {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    background: #0f62fe;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
}
</style>
