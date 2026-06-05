<template>
    <view class="page">
        <view class="header">
            <image class="avatar" src="/static/logo.png" mode="aspectFill" />
            <view class="user-info">
                <text class="nickname">{{ user.nickname || '未登录' }}</text>
                <text class="level">会员等级: {{ user.level || '--' }}</text>
            </view>
        </view>
        <view class="card">
            <view class="balance-row">
                <view class="balance-item" @click="goBalance">
                    <text class="num">¥{{ user.balance || 0 }}</text>
                    <text class="label">余额</text>
                </view>
                <view class="balance-item" @click="goPoints">
                    <text class="num">{{ user.points || 0 }}</text>
                    <text class="label">积分</text>
                </view>
            </view>
        </view>
        <view class="menu-list">
            <view class="menu-item" @click="goOrders">
                <text>我的订单</text>
                <text>></text>
            </view>
            <view class="menu-item" @click="goAddress">
                <text>收货地址</text>
                <text>></text>
            </view>
            <view class="menu-item" @click="goProfile">
                <text>个人资料</text>
                <text>></text>
            </view>
            <view class="menu-item" @click="changeLang">
                <text>语言 / Language</text>
                <text>{{ currentLang }}</text>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return { user: {}, currentLang: 'zh-CN' }
    },
    onShow() { this.loadUser() },
    methods: {
        loadUser() {
            // TODO: API
        },
        goBalance() { uni.navigateTo({ url: '/pages/member/balance' }) },
        goPoints() { uni.navigateTo({ url: '/pages/member/points' }) },
        goOrders() { uni.navigateTo({ url: '/pages/order/list' }) },
        goAddress() { uni.navigateTo({ url: '/pages/address/list' }) },
        goProfile() { uni.navigateTo({ url: '/pages/user/profile' }) },
        changeLang() {
            const langs = ['zh-CN', 'en-US', 'ja-JP']
            uni.showActionSheet({
                itemList: langs,
                success: (res) => {
                    this.currentLang = langs[res.tapIndex]
                    // TODO: 切换语言
                }
            })
        }
    }
}
</script>

<style scoped>
.header {
    display: flex;
    align-items: center;
    padding: 40rpx;
    background: linear-gradient(135deg, #0f62fe, #5e9eff);
}

.avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    border: 4rpx solid #fff;
}

.user-info {
    margin-left: 24rpx;
}

.nickname {
    font-size: 36rpx;
    color: #fff;
    font-weight: bold;
}

.level {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 8rpx;
}

.card {
    margin: -30rpx 20rpx 0;
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
}

.balance-row {
    display: flex;
}

.balance-item {
    flex: 1;
    text-align: center;
}

.num {
    font-size: 40rpx;
    font-weight: bold;
    color: #f53f2c;
}

.label {
    font-size: 24rpx;
    color: #999;
    margin-top: 8rpx;
    display: block;
}

.menu-list {
    margin: 20rpx;
    background: #fff;
    border-radius: 12rpx;
}

.menu-item {
    display: flex;
    justify-content: space-between;
    padding: 28rpx 24rpx;
    font-size: 28rpx;
    border-bottom: 1rpx solid #f5f5f5;
}
</style>
