<template>
    <view class="page">
        <view class="cart-list" v-if="items.length > 0">
            <view class="cart-item" v-for="item in items" :key="item.id">
                <view class="check" @click="toggleCheck(item)">{{ item.checked ? '✓' : '○' }}</view>
                <image src="/static/logo.png" mode="aspectFill" class="cart-img" />
                <view class="cart-info">
                    <text class="cart-name">{{ item.goodsName }}</text>
                    <text class="cart-spec">{{ item.spec }}</text>
                    <view class="cart-bottom">
                        <text class="cart-price">¥{{ item.price }}</text>
                        <view class="qty-ctrl">
                            <text class="qty-btn" @click="changeQty(item, -1)">-</text>
                            <text class="qty-num">{{ item.quantity }}</text>
                            <text class="qty-btn" @click="changeQty(item, 1)">+</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <view v-else class="empty">购物车是空的</view>
        <view class="bottom-bar" v-if="items.length > 0">
            <view class="total">合计: ¥{{ totalPrice }}</view>
            <button class="btn-settle" @click="goConfirm">结算</button>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return { items: [] }
    },
    computed: {
        totalPrice() {
            return this.items.filter(i => i.checked).reduce((s, i) => s + i.price * i.quantity, 0)
        }
    },
    onShow() { this.loadCart() },
    methods: {
        loadCart() {
            // TODO: API
        },
        toggleCheck(item) { item.checked = !item.checked },
        changeQty(item, delta) {
            item.quantity = Math.max(1, item.quantity + delta)
        },
        goConfirm() { uni.navigateTo({ url: '/pages/order/confirm' }) }
    }
}
</script>

<style scoped>
.page {
    padding-bottom: 100rpx;
}

.cart-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: #fff;
    margin: 10rpx 20rpx;
    border-radius: 12rpx;
}

.check {
    width: 48rpx;
    height: 48rpx;
    line-height: 48rpx;
    text-align: center;
    font-size: 32rpx;
    color: #0f62fe;
}

.cart-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 8rpx;
    background: #e5e5e5;
    margin: 0 16rpx;
}

.cart-info {
    flex: 1;
}

.cart-name {
    font-size: 28rpx;
    color: #333;
}

.cart-spec {
    font-size: 24rpx;
    color: #999;
}

.cart-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12rpx;
}

.cart-price {
    font-size: 30rpx;
    color: #f53f2c;
    font-weight: bold;
}

.qty-ctrl {
    display: flex;
    align-items: center;
    gap: 16rpx;
}

.qty-btn {
    width: 48rpx;
    height: 48rpx;
    line-height: 48rpx;
    text-align: center;
    background: #f5f5f5;
    border-radius: 8rpx;
    font-size: 28rpx;
}

.qty-num {
    font-size: 28rpx;
    min-width: 40rpx;
    text-align: center;
}

.empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
}

.bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    background: #fff;
}

.total {
    font-size: 30rpx;
    color: #f53f2c;
    font-weight: bold;
}

.btn-settle {
    height: 72rpx;
    line-height: 72rpx;
    padding: 0 40rpx;
    background: #0f62fe;
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;
}
</style>
