import { reactive, computed } from 'vue'
import builtinLocales from '@/locales/index.js'
import { http } from '@/utils/request.js'

function readStoredLocale() {
    try { return uni.getStorageSync('app-locale') || 'zh-CN' } catch { return 'zh-CN' }
}
function readStoredAvailable() {
    try {
        const raw = uni.getStorageSync('app-locales-available')
        return raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : null
    } catch { return null }
}

const state = reactive({
    currentLocale: readStoredLocale(),
    messages: builtinLocales[readStoredLocale()] || builtinLocales['zh-CN'],
    available: readStoredAvailable() || [
        { code: 'zh-CN', name: '简体中文', enabled: true },
        { code: 'en-US', name: 'English', enabled: true },
        { code: 'ja-JP', name: '日本語', enabled: true }
    ],
    defaultLocale: 'zh-CN',
    initialized: false,
    rev: 0
})

function setMessagesFor(locale) {
    state.messages = builtinLocales[locale] || builtinLocales['zh-CN']
}

function t(key) {
    if (!key) return ''
    void state.rev
    const keys = key.split('.')
    let result = state.messages
    for (const k of keys) result = result?.[k]
    if (result === undefined) {
        let fallback = builtinLocales['zh-CN']
        for (const k of keys) fallback = fallback?.[k]
        return fallback || key
    }
    return result
}

function locale() { return state.currentLocale }

function setLocale(lang) {
    if (!builtinLocales[lang]) {
        console.warn('[i18n] unknown locale, fallback to zh-CN:', lang)
        lang = 'zh-CN'
    }
    state.currentLocale = lang
    setMessagesFor(lang)
    state.rev++
    try { uni.setStorageSync('app-locale', lang) } catch (_) {}
}

function getLocale() { return state.currentLocale }

async function initFromBackend() {
    if (state.initialized) return
    try {
        const res = await http.get('/i18n/locales')
        if (res && res.default && Array.isArray(res.locales)) {
            state.defaultLocale = res.default
            state.available = res.locales.filter(l => l.enabled)
            try { uni.setStorageSync('app-locales-available', JSON.stringify(state.available)) } catch (_) {}
            if (!state.available.find(l => l.code === state.currentLocale)) {
                setLocale(res.default)
            } else {
                state.rev++
            }
        }
    } catch (e) {
        console.warn('[i18n] 后端语言列表拉取失败，使用内置兜底：', e?.message || e)
    } finally {
        state.initialized = true
        state.rev++
    }
}

export function useI18n() {
    return {
        t, setLocale, getLocale, initFromBackend, locale,
        currentLocale: computed(() => state.currentLocale),
        available: computed(() => state.available),
        defaultLocale: computed(() => state.defaultLocale),
        initialized: computed(() => state.initialized)
    }
}

export function useT() {
    const i18n = useI18n()
    return { ...i18n, t, setLocale }
}

export default {
    install(app) {
        const i18n = useI18n()
        app.config.globalProperties.$t = t
        app.config.globalProperties.$locale = locale
        app.config.globalProperties.$setLocale = setLocale
        app.config.globalProperties.$getLocale = getLocale
        app.config.globalProperties.$i18n = i18n
        initFromBackend()
    }
}
