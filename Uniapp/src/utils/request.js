// 集中请求封装
// - 自动注入 Bearer token（uni.storage 同步）
// - 401 清掉登录态并提示
// - 支持 per-call baseURL（默认 /api/app，auth/i18n 用 /api/auth /api/i18n）
export const APP_BASE = '/api/app'
export const AUTH_BASE = '/api/auth'
export const I18N_BASE = '/api/i18n'

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
  // url 已经包含完整 path 如 /api/i18n/locales、/api/auth/login
  if (url.startsWith('/api/')) return url
  const u = url.startsWith('/') ? url : '/' + url
  if (u.startsWith(baseURL)) return u
  return baseURL + u
}

export function request(options = {}) {
  const baseURL = options.baseURL || APP_BASE
  const url = normalizeUrl(options.url || '', baseURL)
  const method = (options.method || 'GET').toUpperCase()
  const data = options.data || {}
  const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
  const token = getToken()
  if (token) header['Authorization'] = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method,
      data,
      header,
      timeout: options.timeout || 15000,
      success(res) {
        const body = res.data || {}
        if (body.code === 200 || body.code === 0) {
          resolve(body.data !== undefined ? body.data : body)
        } else if (body.code === 401) {
          clearLogin()
          uni.showToast({ title: body.message || '请先登录', icon: 'none' })
          reject(body)
        } else {
          uni.showToast({ title: body.message || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
