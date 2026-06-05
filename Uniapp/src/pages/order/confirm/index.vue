<template>
    <view class="page">
        <view class="address" @click="selectAddress">
            <text v-if="address">{{ address.name }} {{ address.phone }}</text>
            <text v-else>请选择收货地址 ></text>
        </view>
        <view class="goods-list">
            <view class="goods-item" v-for="g in goods" :key="g.id">
                <text>{{ g.name }} x{{ g.quantity }}</text>
                <text>¥{{ g.price * g.quantity }}</text>
            </view>
        </view>
        <view class="total">合计: ¥{{ totalPrice }}</view>
        <button class="btn-pay" @click="submit">提交订单</button>
    </view>
</template>

<script>
export default {
    data() {
        return { address: null, goods: [], totalPrice: 0 }
    },
    onLoad() {
        // TODO: 加载选中商品
    },
    methods: {
        selectAddress() { uni.navigateTo({ url: '/pages/address/list' }) },
        submit() { uni.showToast({ title: '已提交', icon: 'success' }) }
    }
}
</script>

<style scoped>
.page {
    padding: 20rpx;
}

.address {
    padding: 24rpx;
    background: #fff;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
}

.goods-item {
    display: flex;
    justify-content: space-between;
    padding: 16rpx;
    background: #fff;
    margin-bottom: 8rpx;
    border-radius: 8rpx;
}

.total {
    text-align: right;
    font-size: 36rpx;
    color: #f53f2c;
    font-weight: bold;
    padding: 20rpx 0;
}

.btn-pay {
    height: 80rpx;
    line-height: 80rpx;
    background: #0f62fe;
    color: #fff;
    border-radius: 40rpx;
    font-size: 32rpx;
}
</style>
