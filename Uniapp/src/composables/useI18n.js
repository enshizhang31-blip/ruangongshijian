import { ref } from 'vue'
import locales from '@/locales/index.js'

// 延迟获取当前语言，避免模块顶层调用 uni API
function getStoredLocale() {
    try { return uni.getStorageSync('app-locale') || 'zh-CN' } catch { return 'zh-CN' }
}

export function useI18n() {
    const currentLocale = ref(getStoredLocale())
    const messages = ref(locales[currentLocale.value] || locales['zh-CN'])

    function t(key) {
        const keys = key.split('.')
        let result = messages.value
        for (const k of keys) result = result?.[k]
        return result || key
    }

    function setLocale(lang) {
        currentLocale.value = lang
        messages.value = locales[lang] || locales['zh-CN']
        uni.setStorageSync('app-locale', lang)
    }

    function getLocale() { return currentLocale.value }

    return { t, setLocale, getLocale, currentLocale }
}

// 全局 mixin
export default {
    install(app) {
        const i18n = useI18n()
        app.config.globalProperties.$t = i18n.t
        app.config.globalProperties.$setLocale = i18n.setLocale
        app.config.globalProperties.$getLocale = i18n.getLocale
    }
}
