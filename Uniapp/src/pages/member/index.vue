<template>
    <view class="page">
        <view class="header" @click="onHeaderClick">
            <image class="avatar" src="/static/logo.png" mode="aspectFill" />
            <view class="user-info">
                <text class="nickname">{{ user.member?.nickname || $t('member.notLoggedIn') }}</text>
                <text v-if="user.isLoggedIn.value" class="level">{{ $t('member.level') }}: {{ user.memberLevelName.value }}</text>
                <text v-else class="level">{{ $t('member.demoAccount') }}</text>
            </view>
        </view>

        <view v-if="user.isLoggedIn.value" class="card">
            <view class="balance-row">
                <view class="balance-item" @click="goBalance">
                    <text class="num">{{ $t('common.yuan') }}{{ formatPrice(user.member?.balance) }}</text>
                    <text class="label">{{ $t('member.balance') }}</text>
                </view>
                <view class="balance-item" @click="goPoints">
                    <text class="num">{{ user.member?.points || 0 }}</text>
                    <text class="label">{{ $t('member.points') }}</text>
                </view>
            </view>
        </view>

        <view class="menu-list">
            <view class="menu-item" @click="goOrders">
                <text>📦 {{ $t('member.orders') }}</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @click="goAddress">
                <text>📍 {{ $t('member.address') }}</text>
                <text class="arrow">›</text>
            </view>
            <view class="menu-item" @click="goDebug">
                <text>🛠 API 调用日志</text>
                <text class="arrow">›</text>
            </view>
        </view>

        <view class="menu-list">
            <view class="menu-item">
                <text>🌐 {{ $t('member.language') }}</text>
            </view>
            <view class="lang-row">
                <text class="lang-pill" v-for="loc in availableLocales" :key="loc.code"
                      :class="{ active: currentLang === loc.code }"
                      @click="switchLang(loc.code)">
                    {{ loc.name }}
                </text>
            </view>
        </view>

        <view class="menu-list">
            <view v-if="user.isLoggedIn.value" class="menu-item danger" @click="onLogout">
                <text>↩ {{ $t('member.logout') }}</text>
            </view>
            <view v-else class="menu-item danger" @click="goLogin">
                <text>🔐 {{ $t('auth.loginTitle') }}</text>
                <text class="arrow">›</text>
            </view>
        </view>

        <view class="tip">{{ $t('member.demoAccount') }}</view>
    </view>
</template>

<script>
import { memberApi } from '@/api/index.js'
import { useUserStore } from '@/stores/index.js'

export default {
    data() { return { currentLang: 'zh-CN', availableLocales: [], __requireLogin: false } },
    computed: { user() { return useUserStore() } },
    onShow() {
        if (this.user.isLoggedIn.value) this.refreshMember()
        this.currentLang = this.$i18n.getLocale()
        const avail = this.$i18n.available
        this.availableLocales = (avail && avail.value) || avail || []
        if (!this.availableLocales.length) {
            this.availableLocales = [
                { code: 'zh-CN', name: '简体中文' },
                { code: 'en-US', name: 'English' },
                { code: 'ja-JP', name: '日本語' }
            ]
        }
    },
    methods: {
        async refreshMember() {
            try { const m = await memberApi.info(); this.user.setMember(m) } catch (e) {}
        },
        onHeaderClick() { if (!this.user.isLoggedIn.value) this.goLogin() },
        goLogin() { uni.navigateTo({ url: '/pages/auth/login/index' }) },
        goBalance() { uni.navigateTo({ url: '/pages/member/balance/index' }) },
        goPoints() { uni.navigateTo({ url: '/pages/member/points/index' }) },
        goOrders() {
            if (this.user.requireLogin()) uni.navigateTo({ url: '/pages/order/list/index' })
        },
        goAddress() { uni.navigateTo({ url: '/pages/address/list/index' }) },
        switchLang(code) {
            this.$setLocale(code)
            this.currentLang = code
            uni.showToast({ title: this.$t('toast.languageChanged'), icon: 'success' })
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        onLogout() {
            this.user.logout()
            uni.showToast({ title: this.$t('common.success'), icon: 'success' })
        }
    }
}
</script>

<style scoped>
.page { padding-bottom: 40rpx; }
.header { display: flex; align-items: center; padding: 60rpx 40rpx 100rpx; background: linear-gradient(135deg, #0f62fe, #5e9eff); }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; border: 6rpx solid #fff; background: #fff; }
.user-info { margin-left: 24rpx; }
.nickname { font-size: 40rpx; color: #fff; font-weight: bold; }
.level { font-size: 26rpx; color: rgba(255, 255, 255, 0.85); margin-top: 8rpx; }
.card { margin: -50rpx 20rpx 0; background: #fff; border-radius: 16rpx; padding: 30rpx; }
.balance-row { display: flex; }
.balance-item { flex: 1; text-align: center; }
.num { font-size: 44rpx; font-weight: bold; color: #f53f2c; }
.label { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.menu-list { margin: 20rpx; background: #fff; border-radius: 12rpx; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 24rpx; font-size: 28rpx; color: #333; border-bottom: 1rpx solid #f5f5f5; }
.menu-item:last-child { border-bottom: none; }
.menu-item.danger { color: #f53f2c; }
.arrow { color: #ccc; font-size: 32rpx; }
.lang-row { display: flex; flex-wrap: wrap; gap: 16rpx; padding: 24rpx; border-top: 1rpx solid #f5f5f5; }
.lang-pill { padding: 12rpx 28rpx; background: #f5f5f5; border-radius: 32rpx; font-size: 26rpx; color: #555; }
.lang-pill.active { background: #0f62fe; color: #fff; }
.tip { margin: 40rpx 40rpx 0; text-align: center; font-size: 24rpx; color: #999; line-height: 1.6; }
</style>
