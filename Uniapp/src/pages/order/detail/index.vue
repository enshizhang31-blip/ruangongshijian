<template>
    <view class="page" v-if="order">
        <view class="card">
            <view class="row"><text>订单号</text><text>{{ order.orderNo }}</text></view>
            <view class="row"><text>状态</text><text class="status">{{ order.status }}</text></view>
            <view class="row"><text>金额</text><text class="price">¥{{ order.totalAmount }}</text></view>
        </view>
        <view class="card">
            <view class="title">商品明细</view>
            <view class="goods-item" v-for="i in order.items" :key="i.id">
                <text>{{ i.goodsName }}</text>
                <text>x{{ i.quantity }} ¥{{ i.price }}</text>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return { order: null }
    },
    onLoad(options) {
        // TODO: 根据 options.id 加载订单详情
    }
}
</script>

<style scoped>
.card {
    margin: 20rpx;
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
}

.row {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 28rpx;
}

.status {
    color: #0f62fe;
}

.price {
    color: #f53f2c;
    font-weight: bold;
}

.title {
    font-size: 30rpx;
    font-weight: bold;
    margin-bottom: 12rpx;
}

.goods-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 28rpx;
    border-top: 1rpx solid #eee;
}
</style>
