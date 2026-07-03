/**
 * 演示版轻量级响应式 store（不依赖 Pinia / vuex）
 */
import { reactive, computed } from 'vue'

const USER_KEY = 'demo-user'
function loadUser() {
    try {
        const raw = uni.getStorageSync(USER_KEY)
        return raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : null
    } catch (_) { return null }
}

let _userState = null
function userState() {
    if (!_userState) {
        const stored = loadUser()
        _userState = reactive({
            token: stored?.token || '',
            member: stored?.member || null
        })
    }
    return _userState
}

export function useUserStore() {
    const state = userState()
    const isLoggedIn = computed(() => !!state.token)
    const isMember = computed(() => isLoggedIn.value && !!state.member)
    const memberLevelName = computed(() => {
        const map = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '钻石会员' }
        return map[state.member?.memberLevel || 1] || '普通会员'
    })

    function login(payload) {
        state.token = payload.token || ''
        state.member = payload.member || null
        uni.setStorageSync(USER_KEY, JSON.stringify({ token: state.token, member: state.member }))
        uni.setStorageSync('token', state.token)
    }

    function setMember(member) {
        state.member = member
        uni.setStorageSync(USER_KEY, JSON.stringify({ token: state.token, member: state.member }))
    }

    function logout() {
        state.token = ''
        state.member = null
        uni.removeStorageSync(USER_KEY)
        uni.removeStorageSync('token')
    }

    function requireLogin(redirect) {
        if (isLoggedIn.value) return true
        const pages = (typeof getCurrentPages === 'function' ? getCurrentPages() : [])
        const cur = pages[pages.length - 1]
        const curPath = cur && (cur.route || (cur.$page && cur.$page.fullPath)) || ''
        // 默认行为：跳登录页 + toast 提示
        uni.showToast({ title: '请先登录', icon: 'none' })
        // 若传 redirect = false，则不跳（只提示）
        if (redirect === false) return false
        const target = redirect || curPath
        const url = target
            ? '/pages/auth/login/index?redirect=' + encodeURIComponent('/' + target.replace(/^\//, ''))
            : '/pages/auth/login/index'
        // 已在登录页就不再跳
        if (cur && cur.route === 'pages/auth/login/index') return false
        setTimeout(() => uni.navigateTo({ url }), 200)
        return false
    }

    return { state, isLoggedIn, isMember, memberLevelName, login, setMember, logout, requireLogin }
}

const CART_KEY = 'demo-cart'
function loadCart() {
    try {
        const raw = uni.getStorageSync(CART_KEY)
        return raw ? (typeof raw === 'string' ? JSON.parse(raw) : raw) : []
    } catch (_) { return [] }
}

let _cartState = null
function cartState() {
    if (!_cartState) {
        _cartState = reactive({ items: loadCart() })
    }
    return _cartState
}

export function useCartStore() {
    const state = cartState()
    const count = computed(() => state.items.reduce((s, i) => s + (i.quantity || 0), 0))
    const total = computed(() => state.items.reduce((s, i) => s + (i.price || 0) * (i.quantity || 0), 0))

    function save() { uni.setStorageSync(CART_KEY, JSON.stringify(state.items)) }

    function addItem(goods) {
        const exist = state.items.find(i => i.spuId === goods.spuId)
        if (exist) {
            exist.quantity += goods.quantity || 1
        } else {
            state.items.push({
                id: Date.now() + Math.floor(Math.random() * 1000),
                spuId: goods.spuId,
                skuId: goods.skuId || goods.spuId,
                goodsName: goods.name,
                price: goods.price,
                quantity: goods.quantity || 1,
                imageUrl: goods.imageUrl || '',
                spec: goods.spec || '',
                checked: true
            })
        }
        save()
    }

    function updateQty(id, quantity) {
        const item = state.items.find(i => i.id === id)
        if (item) { item.quantity = Math.max(1, quantity); save() }
    }

    function remove(id) {
        const idx = state.items.findIndex(i => i.id === id)
        if (idx >= 0) { state.items.splice(idx, 1); save() }
    }

    function clear() { state.items = []; save() }

    function checkedItems() { return state.items.filter(i => i.checked) }

    return { state, count, total, addItem, updateQty, remove, clear, checkedItems, save }
}
