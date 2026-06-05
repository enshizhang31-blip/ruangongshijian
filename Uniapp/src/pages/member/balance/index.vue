<template>
    <view class="page">
        <view class="card">
            <view class="balance">当前余额: <text class="price">¥{{ balance }}</text></view>
            <view class="amount-grid">
                <view class="amount-item" v-for="a in amounts" :key="a" :class="{ active: selected === a }"
                    @click="selected = a">
                    ¥{{ a }}
                </view>
            </view>
            <button class="btn" @click="recharge">确认充值</button>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return { balance: 0, amounts: [50, 100, 200, 500], selected: 50 }
    },
    onShow() { /* TODO: 加载余额 */ },
    methods: {
        recharge() { uni.showToast({ title: '充值成功', icon: 'success' }) }
    }
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
}

.balance {
    font-size: 28rpx;
    color: #666;
    text-align: center;
    margin-bottom: 30rpx;
}

.price {
    font-size: 40rpx;
    color: #f53f2c;
    font-weight: bold;
}

.amount-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-bottom: 30rpx;
}

.amount-item {
    width: calc(50% - 10rpx);
    text-align: center;
    padding: 30rpx 0;
    border: 2rpx solid #e5e5e5;
    border-radius: 12rpx;
    font-size: 32rpx;
}

.amount-item.active {
    border-color: #0f62fe;
    color: #0f62fe;
}

.btn {
    height: 80rpx;
    line-height: 80rpx;
    background: #0f62fe;
    color: #fff;
    border-radius: 40rpx;
    font-size: 32rpx;
}
</style>
