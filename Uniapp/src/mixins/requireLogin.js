/**
 * 全局拦截 mixin：受保护页面引入此 mixin 后，
 * 每次 onShow 自动检查登录态，未登录则跳登录页（带 redirect）。
 *
 * 用法（页面里）：
 *   import requireLogin from '@/mixins/requireLogin.js'
 *   export default { mixins: [requireLogin], ... }
 *
 * 也可在 data / 组件标记：
 *   data() { return { __requireLogin: true } }
 *   不想拦截的页面: __requireLogin: false
 *   自定义跳页: __requireLoginRedirect: '/pages/cart/index'
 */
import { useUserStore } from '@/stores/index.js'

export default {
    onShow() {
        // 允许页面显式关闭
        if (this.__requireLogin === false) return
        const user = useUserStore()
        if (!user.isLoggedIn.value) {
            const redirect = this.__requireLoginRedirect || this.$page?.fullPath || ''
            user.requireLogin(redirect || false)
        }
    }
}
