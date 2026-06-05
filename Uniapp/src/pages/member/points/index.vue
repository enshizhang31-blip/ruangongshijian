<template>
    <view class="page">
        <view class="card">
            <view class="points">当前积分: <text class="num">{{ points }}</text></view>
        </view>
        <view class="goods-grid">
            <view class="goods-item" v-for="g in goods" :key="g.id">
                <image src="/static/logo.png" mode="aspectFill" class="goods-img" />
                <text class="goods-name">{{ g.name }}</text>
                <text class="goods-points">{{ g.points }}积分</text>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return { points: 0, goods: [] }
    },
    onShow() { /* TODO: 加载积分和商品 */ }
}
</script>

<style scoped>
.page {
    padding: 20rpx;
}

.card {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    text-align: center;
}

.points {
    font-size: 28rpx;
    color: #666;
}

.num {
    font-size: 40rpx;
    color: #f53f2c;
    font-weight: bold;
}

.goods-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
}

.goods-item {
    width: calc(50% - 8rpx);
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    text-align: center;
    padding-bottom: 16rpx;
}

.goods-img {
    width: 100%;
    height: 200rpx;
    background: #e5e5e5;
}

.goods-name {
    font-size: 28rpx;
    color: #333;
    margin-top: 12rpx;
}

.goods-points {
    font-size: 26rpx;
    color: #0f62fe;
    margin-top: 8rpx;
}
</style>
