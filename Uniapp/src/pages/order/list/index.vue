<template>
    <view class="page">
        <view class="tabs">
            <view class="tab" :class="{ active: tab === 'pending' }" @click="tab = 'pending'">待付款</view>
            <view class="tab" :class="{ active: tab === 'paid' }" @click="tab = 'paid'">已付款</view>
            <view class="tab" :class="{ active: tab === 'completed' }" @click="tab = 'completed'">已完成</view>
        </view>
        <view class="order-list">
            <view class="order-item" v-for="o in orders" :key="o.id" @click="goDetail(o.id)">
                <view class="order-header">
                    <text>订单号: {{ o.orderNo }}</text>
                    <text class="status">{{ o.status }}</text>
                </view>
                <view class="order-body" v-for="i in o.items" :key="i.id">
                    <text>{{ i.goodsName }} x{{ i.quantity }}</text>
                    <text>¥{{ i.price }}</text>
                </view>
                <view class="order-footer">合计: ¥{{ o.totalAmount }}</view>
            </view>
        </view>
        <view v-if="orders.length === 0" class="empty">暂无订单</view>
    </view>
</template>

<script>
export default {
    data() {
        return { tab: 'pending', orders: [] }
    },
    onShow() { this.loadOrders() },
    methods: {
        loadOrders() {
            // TODO: API
        },
        goDetail(id) { uni.navigateTo({ url: '/pages/order/detail?id=' + id }) }
    }
}
</script>

<style scoped>
.tabs {
    display: flex;
    background: #fff;
}

.tab {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    color: #666;
}

.tab.active {
    color: #0f62fe;
    border-bottom: 4rpx solid #0f62fe;
}

.order-item {
    margin: 16rpx 20rpx;
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
}

.order-header {
    display: flex;
    justify-content: space-between;
    font-size: 26rpx;
    color: #666;
}

.order-body {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 28rpx;
}

.order-footer {
    text-align: right;
    font-size: 30rpx;
    color: #f53f2c;
    font-weight: bold;
}

.empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
}
</style>
