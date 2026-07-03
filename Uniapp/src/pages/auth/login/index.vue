<template>
    <view class="page">
        <view class="logo-area">
            <image src="/static/logo.png" mode="aspectFit" class="logo" />
            <text class="app-name">SaleManager</text>
            <text class="subtitle">{{ $t('home.bannerTip') }}</text>
        </view>

        <view class="card">
            <view class="title">{{ mode === 'login' ? $t('auth.loginTitle') : $t('auth.registerTitle') }}</view>

            <view class="form-item" v-if="mode === 'register'">
                <text class="label">{{ $t('auth.nickname') }}</text>
                <input class="input" v-model="nickname" :placeholder="$t('auth.nicknamePlaceholder')" maxlength="20" />
            </view>

            <view class="form-item">
                <text class="label">{{ $t('auth.phone') }}</text>
                <input class="input" v-model="phone" :placeholder="$t('auth.phonePlaceholder')" maxlength="20" />
            </view>

            <view class="form-item">
                <text class="label">{{ $t('auth.password') }}</text>
                <input class="input" v-model="password" :placeholder="$t('auth.passwordPlaceholder')" password maxlength="32" />
            </view>

            <button class="btn-primary" @click="submit" :loading="loading" :disabled="loading">
                {{ mode === 'login' ? $t('auth.submitLogin') : $t('auth.submitRegister') }}
            </button>

            <view class="switch-bar">
                <text class="link" @click="toggleMode">
                    {{ mode === 'login' ? $t('auth.goRegister') : $t('auth.goLogin') }}
                </text>
                <text class="link demo" @click="fillDemo">{{ $t('auth.demoHint') }}</text>
            </view>
        </view>

        <view class="lang-bar">
            <text class="lang-label">{{ $t('member.currentLang') }}:</text>
            <text class="lang-option" v-for="loc in availableLocales" :key="loc.code"
                  :class="{ active: currentLang === loc.code }"
                  @click="switchLang(loc.code)">
                {{ loc.code }}
            </text>
        </view>
    </view>
</template>

<script>
import { authApi } from '@/api/index.js'
import { useUserStore } from '@/stores/index.js'

export default {
    data() {
        return {
            mode: 'login',
            phone: '',
            password: '',
            nickname: '',
            loading: false,
            currentLang: 'zh-CN',
            availableLocales: []
        }
    },
    onLoad() {
        this.currentLang = this.$i18n.getLocale()
        const avail = this.$i18n.available
        this.availableLocales = (avail && avail.value) || avail || []
    },
    methods: {
        fillDemo() { this.phone = 'demo'; this.password = '123456'; this.mode = 'login' },
        toggleMode() { this.mode = this.mode === 'login' ? 'register' : 'login' },
        switchLang(code) {
            this.$setLocale(code)
            this.currentLang = code
            uni.showToast({ title: this.$t('toast.languageChanged'), icon: 'success' })
        },
        async submit() {
            if (!this.phone || !this.password) {
                return uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' })
            }
            if (this.mode === 'register' && this.password.length < 6) {
                return uni.showToast({ title: this.$t('auth.password') + ' ≥ 6', icon: 'none' })
            }
            this.loading = true
            try {
                const res = this.mode === 'login'
                    ? await authApi.loginByPhone(this.phone, this.password)
                    : await authApi.register(this.phone, this.password, this.nickname)
                const data = res && (res.token ? res : res.data) ? (res.token ? res : res.data) : res
                 const token = data.token || (data.data && data.data.token)
                 const member = data.member || (data.data && data.data.member)
                 if (token) {
                     useUserStore().login({ token, member })
                     uni.setStorageSync('token', token)
                     uni.setStorageSync('member', member || null)
                     uni.setStorageSync('demo-token', token)
                     uni.setStorageSync('demo-member', member || null)
                 }
                uni.showToast({ title: this.$t(this.mode === 'login' ? 'auth.loginSuccess' : 'auth.registerSuccess'), icon: 'success' })
                setTimeout(() => {
                    if (this.redirectTarget) {
                        // tabBar 页面必须 switchTab，其它用 redirectTo
                        const tabBarPages = [
                            'pages/index/index',
                            'pages/goods/list/index',
                            'pages/cart/index',
                            'pages/member/index'
                        ]
                        const pathNoQuery = this.redirectTarget.split('?')[0].replace(/^\//, '')
                        if (tabBarPages.indexOf(pathNoQuery) >= 0) {
                            uni.switchTab({ url: this.redirectTarget })
                        } else if (this.redirectTarget !== '/pages/index/index') {
                            uni.redirectTo({ url: this.redirectTarget })
                        } else {
                            uni.reLaunch({ url: '/pages/index/index' })
                        }
                    } else {
                        uni.reLaunch({ url: '/pages/index/index' })
                    }
                }, 600)
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('toast.networkError'), icon: 'none' })
            } finally {
                this.loading = false
            }
        }
    }
}
</script>

<style scoped>
.page {
    min-height: 100vh;
    background: linear-gradient(180deg, #0f62fe 0%, #5e9eff 30%, #F5F5F5 100%);
    padding: 60rpx 40rpx 40rpx;
    box-sizing: border-box;
}
.logo-area { display: flex; flex-direction: column; align-items: center; margin-bottom: 60rpx; }
.logo { width: 120rpx; height: 120rpx; border-radius: 24rpx; background: #fff; margin-bottom: 16rpx; }
.app-name { font-size: 44rpx; font-weight: bold; color: #fff; }
.subtitle { font-size: 26rpx; color: rgba(255, 255, 255, 0.85); margin-top: 8rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 48rpx 32rpx; box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08); }
.title { font-size: 40rpx; font-weight: bold; color: #333; text-align: center; margin-bottom: 40rpx; }
.form-item { margin-bottom: 28rpx; }
.label { font-size: 26rpx; color: #666; margin-bottom: 12rpx; }
.input { width: 100%; height: 80rpx; padding: 0 24rpx; background: #f7f7f7; border-radius: 12rpx; font-size: 30rpx; box-sizing: border-box; }
.btn-primary { width: 100%; height: 88rpx; line-height: 88rpx; background: #0f62fe; color: #fff; border-radius: 44rpx; font-size: 32rpx; margin-top: 24rpx; border: none; }
.btn-primary[disabled] { background: #97baff; }
.switch-bar { display: flex; justify-content: space-between; margin-top: 32rpx; }
.link { font-size: 26rpx; color: #0f62fe; }
.link.demo { color: #f53f2c; }
.lang-bar { display: flex; align-items: center; justify-content: center; margin-top: 40rpx; gap: 16rpx; flex-wrap: wrap; }
.lang-label { font-size: 24rpx; color: #fff; }
.lang-option { padding: 6rpx 18rpx; background: rgba(255, 255, 255, 0.25); color: #fff; border-radius: 24rpx; font-size: 24rpx; }
.lang-option.active { background: #fff; color: #0f62fe; font-weight: bold; }
</style>
