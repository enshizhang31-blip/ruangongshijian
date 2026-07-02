<template>
    <view class="page">
        <view class="card">
            <view class="card-title">{{ $t('order.refundTitle') }}</view>
            <view class="row">
                <text class="row-label">{{ $t('order.orderNo') }}</text>
                <text class="row-value">{{ orderId || '--' }}</text>
            </view>
            <view class="row">
                <text class="row-label">{{ $t('order.refundAmount') }}</text>
                <text class="row-value highlight">{{ $t('common.yuan') }}{{ amount }}</text>
            </view>
        </view>

        <view class="card">
            <view class="card-title">{{ $t('order.refundReason') }}</view>
            <textarea class="reason" v-model="reason" :placeholder="$t('order.refundReasonPlaceholder')" maxlength="200" />
            <view class="counter">{{ reason.length }} / 200</view>
        </view>

        <view style="height: 160rpx;" />

        <view class="footer">
            <view class="submit-btn" @click="submit">
                <text>{{ $t('order.refundSubmit') }}</text>
            </view>
        </view>
    </view>
</template>

<script>
import { orderApi } from '@/api/index.js'

export default {
    data() { return { orderId: null, reason: '', amount: '0.00', loading: false } },
    onLoad(options) {
        this.orderId = options.id
        const order = uni.getStorageSync(`demo-order-${options.id}`)
        if (order) this.amount = Number(order.totalAmount || 0).toFixed(2)
    },
    methods: {
        async submit() {
            if (!this.reason.trim()) {
                uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' })
                return
            }
            this.loading = true
            try { await orderApi.refund(this.orderId, { reason: this.reason, amount: this.amount }) } catch (_) {}
            uni.showToast({ title: this.$t('toast.refundSubmitted'), icon: 'success' })
            setTimeout(() => uni.navigateBack({ delta: 1 }), 600)
            this.loading = false
        }
    }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F5F5F5; padding: 16rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 16rpx; }
.card-title { font-size: 30rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
.row { display: flex; justify-content: space-between; padding: 12rpx 0; font-size: 28rpx; }
.row-label { color: #999; }
.row-value { color: #333; }
.row-value.highlight { color: #f53f2c; font-weight: bold; }
.reason { width: 100%; height: 200rpx; background: #f7f7f7; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; box-sizing: border-box; }
.counter { text-align: right; font-size: 22rpx; color: #999; margin-top: 8rpx; }
.footer { position: fixed; bottom: 0; left: 0; right: 0; padding: 20rpx 32rpx; background: #fff; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04); }
.submit-btn { height: 88rpx; line-height: 88rpx; background: #f53f2c; color: #fff; text-align: center; border-radius: 44rpx; font-size: 32rpx; }
</style>
