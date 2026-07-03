<template>
    <view class="page" v-if="goods">
        <swiper class="banner" :indicator-dots="true" :autoplay="true" :circular="true">
            <swiper-item v-for="(img, idx) in images" :key="idx">
                <image :src="img" mode="aspectFill" class="banner-img" />
            </swiper-item>
        </swiper>

        <view class="info-card">
            <view class="price-row">
                <text class="price-symbol">{{ $t('common.yuan') }}</text>
                <text class="price-num">{{ formatPrice(goodsPrice()) }}</text>
            </view>
            <view class="goods-name">{{ goodsName() }}</view>
            <view v-if="goodsBrand()" class="goods-brand">{{ goodsBrand() }}</view>
            <view class="meta-row">
                <text class="meta">{{ $t('goods.sales') }} {{ goodsSales() }}</text>
                <text class="meta">{{ $t('goods.stock') }} {{ goodsStock() }}</text>
            </view>
        </view>

        <!-- 规格点选 -->
        <view v-if="goods.specs && goods.specs.length" class="card spec-card">
            <view v-for="spec in goods.specs" :key="spec.id" class="spec-row">
                <text class="spec-label">{{ spec.name }}</text>
                <view class="spec-values">
                    <view
                        v-for="v in spec.values"
                        :key="v.id"
                        class="spec-tag"
                        :class="{ active: isSpecSelected(spec.id, v.id), disabled: !isSpecAvailable(spec.id, v.id) }"
                        @click="selectSpec(spec.id, v.id)">
                        <text>{{ v.value }}</text>
                    </view>
                </view>
            </view>
            <!-- 已选规格文本 -->
            <view v-if="selectedSpecText" class="spec-selected">
                <text class="spec-selected-label">已选：</text>
                <text class="spec-selected-text">{{ selectedSpecText }}</text>
            </view>
            <view v-if="currentSku" class="spec-current">
                <text class="spec-current-label">本规格：</text>
                <text class="spec-current-price">¥{{ formatPrice(currentSku.price) }}</text>
                <text class="spec-current-stock">库存 {{ currentSku.stock || 0 }}</text>
                <text v-if="currentSku.unit" class="spec-current-unit">/ {{ currentSku.unit }}</text>
            </view>
        </view>
        <!-- fallback: 没有 specs 字段时（旧版本后端）显示原始 SKU 列表 -->
        <view v-else-if="goods.skus && goods.skus.length" class="card sku-card">
            <view class="card-title">规格</view>
            <view class="sku-list">
                <view v-for="sku in goods.skus" :key="sku.id" class="sku-item" :class="{ active: currentSkuId === sku.id }" @click="selectSku(sku.id)">
                    <view class="sku-row">
                        <text class="sku-name">{{ sku.specText || sku.skuCode || 'SKU #' + sku.id }}</text>
                        <text class="sku-price">¥{{ formatPrice(sku.price) }}</text>
                    </view>
                    <view class="sku-row">
                        <text class="sku-stock">库存 {{ sku.stock || 0 }}</text>
                        <text v-if="sku.unit" class="sku-unit">/ {{ sku.unit }}</text>
                    </view>
                </view>
            </view>
        </view>

        <view class="card qty-card">
            <text class="card-label">{{ $t('goods.qty') }}</text>
            <view class="qty-ctrl">
                <text class="qty-btn" @click="decQty">-</text>
                <text class="qty-num">{{ quantity }}</text>
                <text class="qty-btn" @click="incQty">+</text>
            </view>
        </view>

        <view class="card">
            <view class="card-title">{{ $t('goods.desc') }}</view>
            <view class="desc-body">
                <text v-if="goodsDescription()">{{ goodsDescription() }}</text>
                <text v-else class="empty">{{ $t('goods.noDesc') }}</text>
            </view>
        </view>

        <view style="height: 160rpx;" />

        <view class="footer">
            <view class="btn btn-cart" @click="addToCart">
                <text class="btn-icon">🛒</text>
                <text>{{ $t('goods.addToCart') }}</text>
            </view>
            <view class="btn btn-buy" @click="buyNow">
                <text>{{ $t('goods.buyNow') }}</text>
            </view>
        </view>
    </view>
    <view v-else-if="loadError" class="loading-page">
        <text>😢 {{ loadError }}</text>
    </view>
    <view v-else class="loading-page">
        <text>{{ $t('common.loading') }}</text>
    </view>
</template>

<script>
import { spuApi, cartApi } from '@/api/index.js'
import { useUserStore, useCartStore } from '@/stores/index.js'

export default {
    data() { return { goods: null, quantity: 1, images: [], __requireLogin: false, loadError: null, selectedSpecs: {}, currentSkuId: null } },
    computed: {
        /** 当前选中的规格组合对应的 SKU */
        currentSku() {
            const skus = (this.goods && this.goods.skus) || []
            if (!skus.length) return null
            // 1. 如果有 currentSkuId，优先用它
            if (this.currentSkuId) {
                const hit = skus.find(s => s.id === this.currentSkuId)
                if (hit) return hit
            }
            // 2. 否则用 selectedSpecs 匹配
            if (Object.keys(this.selectedSpecs).length === (this.goods.specs || []).length) {
                const matchSku = skus.find(sku => this.matchSkuSpec(sku))
                if (matchSku) return matchSku
            }
            return null
        },
        selectedSpecText() {
            const specs = (this.goods && this.goods.specs) || []
            const parts = []
            for (const spec of specs) {
                const vId = this.selectedSpecs[spec.id]
                if (vId) {
                    const v = (spec.values || []).find(x => x.id === vId)
                    if (v) parts.push(spec.name + ':' + v.value)
                }
            }
            return parts.join(' / ')
        }
    },
    onLoad(options) {
        if (options.id) this.loadDetail(options.id)
        else uni.showToast({ title: 'missing id', icon: 'none' })
    },
    methods: {
        /** 检查 sku 的 specJson 是否匹配当前 selectedSpecs */
        matchSkuSpec(sku) {
            const sj = sku.specJson
            if (!sj) return false
            try {
                const map = JSON.parse(sj)
                for (const specId in this.selectedSpecs) {
                    const v = String(this.selectedSpecs[specId])
                    if (String(map[specId]) !== v) return false
                }
                return true
            } catch (e) { return false }
        },
        isSpecSelected(specId, valueId) {
            return this.selectedSpecs[specId] === valueId
        },
        /** 该 valueId 在当前其他已选规格下是否有可用 SKU（不灰显） */
        isSpecAvailable(specId, valueId) {
            const skus = (this.goods && this.goods.skus) || []
            if (!skus.length) return true
            // 把当前要检查的 specId/valueId 临时加入选择
            const tmp = Object.assign({}, this.selectedSpecs, { [specId]: valueId })
            // 至少存在一个 SKU 满足 tmp 中所有 spec
            return skus.some(sku => {
                if (!sku.specJson) return true
                try {
                    const map = JSON.parse(sku.specJson)
                    for (const k in tmp) {
                        if (String(map[k]) !== String(tmp[k])) return false
                    }
                    return true
                } catch (e) { return false }
            })
        },
        selectSpec(specId, valueId) {
            // 已选则取消，未选则替换
            if (this.selectedSpecs[specId] === valueId) {
                delete this.selectedSpecs[specId]
            } else {
                this.selectedSpecs[specId] = valueId
            }
            // 触发响应式
            this.selectedSpecs = Object.assign({}, this.selectedSpecs)
        },
        selectSku(skuId) {
            this.currentSkuId = skuId
        },
        async loadDetail(id) {
            this.loadError = null
            try {
                const detail = await spuApi.detail(id)
                if (!detail) {
                    this.loadError = '商品不存在'
                    return
                }
                this.goods = detail
                this.images = []
                if (this.goods.imageUrl) this.images.push(this.goods.imageUrl)
                if (this.goods.mainImage) this.images.push(this.goods.mainImage)
                if (this.goods.images) {
                    if (typeof this.goods.images === 'string') {
                        this.goods.images.split(',').map(s => s.trim()).filter(Boolean).forEach(x => this.images.push(x))
                    } else if (Array.isArray(this.goods.images)) {
                        this.images.push(...this.goods.images)
                    }
                }
                if (!this.images.length) this.images = ['/static/logo.png']
                // 默认选中第一个规格值
                this.initDefaultSpecs()
            } catch (e) {
                this.loadError = (e && e.message) || this.$t('toast.networkError')
                uni.showToast({ title: this.loadError, icon: 'none' })
            }
        },
        /**
         * 智能默认选中规格：
         * 1. 优先选第一个有效组合（按 specId 升序，每个取第一个 valueId）
         * 2. 优先选有库存（stock > 0）的组合
         */
        initDefaultSpecs() {
            this.selectedSpecs = {}
            this.currentSkuId = null
            const specs = (this.goods && this.goods.specs) || []
            if (!specs.length) return
            // 1. 收集所有有库存的 SKU
            const skus = (this.goods.skus || []).filter(s => (s.stock || 0) > 0)
            // 2. 优先：有库存的 SKU 中挑一个 specJson 作参考
            const target = skus[0] || (this.goods.skus || [])[0]
            if (!target || !target.specJson) {
                // 没有任何 SKU 信息可参考，退回到"每个 spec 选第一个 value"
                for (const spec of specs) {
                    if (spec.values && spec.values.length) {
                        this.selectedSpecs[spec.id] = spec.values[0].id
                    }
                }
                this.selectedSpecs = Object.assign({}, this.selectedSpecs)
                return
            }
            // 3. 解析 target.specJson 作为默认选中
            try {
                const map = JSON.parse(target.specJson)
                for (const spec of specs) {
                    const v = map[String(spec.id)]
                    if (v != null) {
                        // 转 number
                        this.selectedSpecs[spec.id] = Number(v)
                    } else if (spec.values && spec.values.length) {
                        // target 不含此 specId 时，退回第一个 value
                        this.selectedSpecs[spec.id] = spec.values[0].id
                    }
                }
                this.selectedSpecs = Object.assign({}, this.selectedSpecs)
            } catch (e) {
                // 解析失败时的兜底
                for (const spec of specs) {
                    if (spec.values && spec.values.length) {
                        this.selectedSpecs[spec.id] = spec.values[0].id
                    }
                }
                this.selectedSpecs = Object.assign({}, this.selectedSpecs)
            }
        },
        /**
         * 商品名称兜底：数据库乱码时显示 spuId
         */
        goodsName() {
            const n = this.goods && this.goods.name
            if (!n) return '商品 #' + (this.goods && this.goods.id)
            return n
        },
        goodsBrand() {
            const b = this.goods && this.goods.brand
            if (!b) return ''
            return b
        },
        goodsDescription() {
            const d = this.goods && this.goods.description
            if (d) return d
            // 从 SKU spec 拼一些说明
            const skus = this.goods && this.goods.skus
            if (Array.isArray(skus) && skus.length) {
                return '共 ' + skus.length + ' 个规格可选'
            }
            return ''
        },
        formatPrice(p) { return Number(p || 0).toFixed(2) },
        goodsPrice() {
            const skus = this.goods && this.goods.skus
            if (Array.isArray(skus) && skus.length) return Math.min(...skus.map(s => Number(s.price || 0)))
            return Number((this.goods && this.goods.price) || 0)
        },
        goodsStock() {
            return (this.goods && (this.goods.stock != null ? this.goods.stock : this.goods.stockCount)) || 0
        },
        goodsSales() {
            return (this.goods && this.goods.salesCount) || 0
        },
        incQty() { this.quantity++ },
        decQty() { if (this.quantity > 1) this.quantity-- },
        ensureLogin() {
            const user = useUserStore()
            if (!user.isLoggedIn.value) { uni.navigateTo({ url: '/pages/auth/login/index' }); return false }
            return true
        },
        firstSku() {
            const skus = this.goods && this.goods.skus
            return Array.isArray(skus) && skus.length ? skus[0] : null
        },
        async addToCart() {
            if (!this.ensureLogin()) return
            const sku = this.currentSku || this.firstSku()
            if (!sku) {
                uni.showToast({ title: '该商品暂无可售 SKU', icon: 'none' })
                return
            }
            // 如果有规格（specs）但没选完，提示
            if ((this.goods.specs || []).length && !this.currentSku) {
                uni.showToast({ title: '请先选择完整规格', icon: 'none' })
                return
            }
            if ((sku.stock || 0) <= 0) {
                uni.showToast({ title: '库存不足', icon: 'none' })
                return
            }
            const cart = useCartStore()
            cart.addItem({
                spuId: this.goods.id, skuId: sku.id,
                name: this.goodsName(), price: Number(sku.price || 0),
                quantity: this.quantity, imageUrl: sku.imageUrl || (this.images && this.images[0]) || '',
                spec: sku.specText || sku.specJson || ''
            })
            try {
                await cartApi.add({ spuId: this.goods.id, skuId: sku.id, quantity: this.quantity })
                uni.showToast({ title: this.$t('toast.addedToCart'), icon: 'success' })
            } catch (e) {
                uni.showToast({ title: '已加入本地购物车', icon: 'none' })
            }
        },
        async buyNow() {
            if (!this.ensureLogin()) return
            const sku = this.currentSku || this.firstSku()
            if (!sku) {
                uni.showToast({ title: '该商品暂无可售 SKU', icon: 'none' })
                return
            }
            if ((this.goods.specs || []).length && !this.currentSku) {
                uni.showToast({ title: '请先选择完整规格', icon: 'none' })
                return
            }
            if ((sku.stock || 0) <= 0) {
                uni.showToast({ title: '库存不足', icon: 'none' })
                return
            }
            const cart = useCartStore()
            // 清理旧临时项
            cart.clear()
            cart.addItem({
                spuId: this.goods.id, skuId: sku.id,
                name: this.goodsName(), price: Number(sku.price || 0),
                quantity: this.quantity, imageUrl: sku.imageUrl || (this.images && this.images[0]) || '',
                spec: sku.specText || sku.specJson || '', checked: true
            })
            try { await cartApi.add({ spuId: this.goods.id, skuId: sku.id, quantity: this.quantity }) } catch (_) {}
            uni.navigateTo({ url: '/pages/order/confirm/index' })
        }
    }
}
</script>

<style scoped>
.page { padding-bottom: 20rpx; background: #F5F5F5; min-height: 100vh; }
.banner { width: 100%; height: 600rpx; background: #fff; }
.banner-img { width: 100%; height: 600rpx; }
.info-card { background: #fff; padding: 32rpx 24rpx; margin-bottom: 16rpx; }
.price-row { display: flex; align-items: baseline; color: #f53f2c; }
.price-symbol { font-size: 28rpx; }
.price-num { font-size: 56rpx; font-weight: bold; }
.goods-name { font-size: 32rpx; color: #333; margin-top: 16rpx; line-height: 1.4; }
.meta-row { display: flex; gap: 32rpx; margin-top: 16rpx; }
.meta { font-size: 24rpx; color: #999; }
.card { background: #fff; margin: 16rpx 0; padding: 24rpx; }
.card-label, .card-title { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 16rpx; }
.qty-card { display: flex; justify-content: space-between; align-items: center; }
.qty-ctrl { display: flex; align-items: center; gap: 24rpx; }
.qty-btn { width: 60rpx; height: 60rpx; line-height: 60rpx; text-align: center; background: #f5f5f5; border-radius: 12rpx; font-size: 32rpx; }
.qty-num { min-width: 60rpx; text-align: center; font-size: 30rpx; }
.desc-body { font-size: 28rpx; color: #555; line-height: 1.6; }
.empty { color: #999; }
.goods-brand { font-size: 24rpx; color: #999; margin-top: 6rpx; }
.sku-card .sku-list { display: flex; flex-direction: column; gap: 12rpx; }
.sku-item { padding: 16rpx; background: #f7f7f7; border-radius: 12rpx; }
.sku-item.active { background: #fff7e6; border: 2rpx solid #f53f2c; }
.sku-row { display: flex; justify-content: space-between; align-items: center; }
.sku-row + .sku-row { margin-top: 6rpx; }
.sku-name { font-size: 28rpx; color: #333; }
.sku-price { font-size: 28rpx; color: #f53f2c; font-weight: bold; }
.sku-stock { font-size: 24rpx; color: #999; }
.sku-unit { font-size: 24rpx; color: #999; }

/* 规格点选 */
.spec-card .spec-row { display: flex; align-items: flex-start; margin-bottom: 20rpx; }
.spec-card .spec-row:last-of-type { margin-bottom: 0; }
.spec-label { width: 80rpx; flex-shrink: 0; font-size: 28rpx; color: #333; padding-top: 12rpx; }
.spec-values { flex: 1; display: flex; flex-wrap: wrap; gap: 16rpx; }
.spec-tag { padding: 12rpx 24rpx; border-radius: 30rpx; border: 2rpx solid #e5e5e5; background: #f7f7f7; font-size: 26rpx; color: #333; }
.spec-tag.active { background: #fff7e6; border-color: #f53f2c; color: #f53f2c; }
.spec-tag.disabled { background: #fafafa; color: #ccc; border-color: #eee; text-decoration: line-through; }
.spec-selected { margin-top: 16rpx; padding-top: 16rpx; border-top: 1rpx dashed #eee; font-size: 26rpx; }
.spec-selected-label { color: #999; }
.spec-selected-text { color: #f53f2c; }
.spec-current { margin-top: 12rpx; font-size: 26rpx; color: #333; }
.spec-current-label { color: #999; }
.spec-current-price { color: #f53f2c; font-weight: bold; font-size: 30rpx; margin-left: 8rpx; }
.spec-current-stock { color: #666; margin-left: 16rpx; }
.spec-current-unit { color: #999; margin-left: 4rpx; }
.footer { position: fixed; bottom: 0; left: 0; right: 0; display: flex; background: #fff; border-top: 1rpx solid #eee; box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04); }
.btn { flex: 1; height: 96rpx; display: flex; align-items: center; justify-content: center; gap: 8rpx; font-size: 28rpx; }
.btn-icon { font-size: 32rpx; }
.btn-cart { color: #333; background: #fff; border-right: 1rpx solid #eee; }
.btn-buy { color: #fff; background: linear-gradient(90deg, #f53f2c, #ff7d00); }
.loading-page { padding: 200rpx 0; text-align: center; color: #999; }
</style>
