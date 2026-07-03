// 集中请求封装
// - 自动注入 Bearer token（uni.storage 同步）
// - 401 清掉登录态并提示
// - 支持 per-call baseURL（默认 /api/app，auth/i18n 用 /api/auth /api/i18n）
//
// 演示版强制走本机后端（127.0.0.1:8080），避免 HBuilderX / Vite 编译差异
// 带来「相对路径 → invalid url」的问题。
const DEV_HOST = 'http://127.0.0.1:8080'
const __IS_MP = true   // 演示版：小程序强制用绝对地址访问本机后端

export const APP_BASE = __IS_MP ? DEV_HOST + '/api/app' : '/api/app'
export const AUTH_BASE = __IS_MP ? DEV_HOST + '/api/auth' : '/api/auth'
export const I18N_BASE = __IS_MP ? DEV_HOST + '/api/i18n' : '/api/i18n'

function getToken() {
  try { return uni.getStorageSync('demo-token') || '' } catch (e) { return '' }
}

export function setLogin(token, member) {
  try {
    uni.setStorageSync('demo-token', token || '')
    uni.setStorageSync('demo-member', member || null)
  } catch (e) {}
}

export function clearLogin() {
  try {
    uni.removeStorageSync('demo-token')
    uni.removeStorageSync('demo-member')
  } catch (e) {}
}

export function isLoggedIn() { return !!getToken() }

export function getMember() {
  try { return uni.getStorageSync('demo-member') || null } catch (e) { return null }
}

function normalizeUrl(url, baseURL) {
    if (!url) return baseURL
    if (url.startsWith('http')) return url
    // url 已经包含完整 path，如 /api/auth/login-by-phone、/api/i18n/locales
    // 演示版：所有 /api/ 前缀的请求都强制走 DEV_HOST，避免小程序相对路径 fail
    if (url.startsWith('/api/')) {
        // baseURL 已是绝对 URL：直接拼接
        if (baseURL.startsWith('http')) {
            return baseURL.replace(/\/api\/[^/]*$/, '') + url
        }
        // baseURL 是相对路径（如 /api/app），强制用 DEV_HOST
        const m = url.match(/^\/api\/([^/]+)/)
        if (m) return DEV_HOST + url
        return DEV_HOST + url
    }
    const u = url.startsWith('/') ? url : '/' + url
    if (u.startsWith(baseURL)) return u
    return baseURL + u
}

// ============================================================
// 调用日志：模仿管理端 axios 拦截器
//   - console 输出（开发者在 VConsole 看到）
//   - 内存中保留最近 200 条（debug 页面读取展示）
// ============================================================
const __LOG_MAX = 200
const __logs = []
let __logSubscribers = []

function pushLog(entry) {
    __logs.unshift(entry)
    if (__logs.length > __LOG_MAX) __logs.length = __LOG_MAX
    __logSubscribers.forEach((fn) => { try { fn(entry) } catch (_) {} })
}

export function getApiLogs() { return __logs.slice() }
export function clearApiLogs() { __logs.length = 0; __logSubscribers.forEach((fn) => { try { fn(null) } catch (_) {} }) }
export function subscribeApiLog(fn) { __logSubscribers.push(fn); return () => { __logSubscribers = __logSubscribers.filter((x) => x !== fn) } }

function safeStringify(o) {
    try { return typeof o === 'string' ? o : JSON.stringify(o) } catch (_) { return String(o) }
}

// ============================================================
// 401 跳转：把当前页面路径带回登录页，登录成功后自动回跳
// ============================================================
function getCurrentPagePath() {
    try {
        const pages = (typeof getCurrentPages === 'function' ? getCurrentPages() : [])
        if (pages && pages.length) {
            const cur = pages[pages.length - 1]
            return (cur && (cur.route || (cur.$page && cur.$page.fullPath))) || ''
        }
    } catch (_) {}
    return ''
}

function redirectToLogin(from) {
    try {
        const url = from
            ? '/pages/auth/login/index?redirect=' + encodeURIComponent('/' + from.replace(/^\//, ''))
            : '/pages/auth/login/index'
        // 已在登录页就不再跳
        const pages = (typeof getCurrentPages === 'function' ? getCurrentPages() : [])
        const cur = pages[pages.length - 1]
        if (cur && cur.route === 'pages/auth/login/index') return
        uni.navigateTo({ url })
    } catch (_) {}
}

export { getCurrentPagePath, redirectToLogin }

export function request(options = {}) {
    const baseURL = options.baseURL || APP_BASE
    const url = normalizeUrl(options.url || '', baseURL)
    const method = (options.method || 'GET').toUpperCase()
    let data = options.data || {}
    // 清理掉 undefined / null / 空字符串
    //   目的：避免 uni.request 把 undefined 序列化为 "undefined" 字符串
    //   Spring 后端 @RequestParam Long 收到 "undefined" 会 NumberFormatException
    const clean = (v) => {
        if (v === undefined || v === null || v === '') return undefined
        if (Array.isArray(v)) return v.map(clean).filter(x => x !== undefined)
        if (v && typeof v === 'object') {
            const out = {}
            Object.keys(v).forEach(k => {
                const cv = clean(v[k])
                if (cv !== undefined) out[k] = cv
            })
            return out
        }
        return v
    }
    data = clean(data) || {}
    const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
    const token = getToken()
    if (token) header['Authorization'] = 'Bearer ' + token

    // 调试：开始
    const __startedAt = Date.now()
    const __reqEntry = {
        id: __startedAt + '-' + Math.random().toString(36).slice(2, 8),
        method,
        url,
        baseURL,
        request: { data: safeStringify(data), header: Object.assign({}, header) },
        response: null,
        status: 'pending',
        costMs: 0,
        startedAt: __startedAt,
        finishedAt: null
    }
    pushLog(__reqEntry)
    console.log(`[请求] ${method} ${url}`, data || '')

    return new Promise((resolve, reject) => {
        uni.request({
            url,
            method,
            data,
            header,
            timeout: options.timeout || 15000,
            success(res) {
                const body = res.data || {}
                const __costMs = Date.now() - __startedAt
                __reqEntry.response = { statusCode: res.statusCode, body: safeStringify(body) }
                __reqEntry.status = body.code === 200 || body.code === 0 ? 'success'
                    : body.code === 401 ? 'unauth' : 'bizError'
                __reqEntry.costMs = __costMs
                __reqEntry.finishedAt = Date.now()
                pushLog(__reqEntry)
                console.log(`[响应] ${method} ${url} (${__costMs}ms)`, body)

                if (body.code === 200 || body.code === 0) {
                    resolve(body.data !== undefined ? body.data : body)
                } else if (body.code === 401) {
                    clearLogin()
                    // 全局拦截：直接跳登录页（带 redirect 回跳路径）
                    if (typeof options.onUnauth === 'function') {
                        try { options.onUnauth(body) } catch (_) {}
                    } else {
                        redirectToLogin(getCurrentPagePath())
                    }
                    uni.showToast({ title: body.message || '请先登录', icon: 'none' })
                    reject(body)
                } else {
                    uni.showToast({ title: body.message || '请求失败', icon: 'none' })
                    reject(body)
                }
            },
            fail(err) {
                const __costMs = Date.now() - __startedAt
                __reqEntry.response = { error: safeStringify(err) }
                __reqEntry.status = 'fail'
                __reqEntry.costMs = __costMs
                __reqEntry.finishedAt = Date.now()
                pushLog(__reqEntry)
                console.error(`[响应错误] ${method} ${url} (${__costMs}ms)`, err)
                uni.showToast({ title: '网络异常', icon: 'none' })
                reject(err)
            }
        })
    })
}

export default request

/**
 * http：axios-like 简洁调用
 *   http.get(url) / http.post(url, data) / http.delete(url) / http.put(url, data)
 *   默认 baseURL = APP_BASE (/api/app)。如要调其它域，请传第二个参数 { baseURL: '/api/i18n' }。
 */
export const http = {
    get(url, options = {}) {
        return request({ ...options, url, method: 'GET' })
    },
    post(url, data, options = {}) {
        return request({ ...options, url, method: 'POST', data })
    },
    put(url, data, options = {}) {
        return request({ ...options, url, method: 'PUT', data })
    },
    delete(url, options = {}) {
        return request({ ...options, url, method: 'DELETE' })
    }
}
