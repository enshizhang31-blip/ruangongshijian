<template>
    <view class="page">
        <view class="header">
            <view class="actions">
                <button class="btn ghost" size="mini" @click="refresh">刷新</button>
                <button class="btn ghost" size="mini" @click="clearAll">清空</button>
                <button class="btn ghost" size="mini" @click="copyAll">复制</button>
            </view>
            <view class="filters">
                <text class="filter" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部 {{ counts.all }}</text>
                <text class="filter" :class="{ active: filter === 'success' }" @click="filter = 'success'">成功 {{ counts.success }}</text>
                <text class="filter" :class="{ active: filter === 'bizError' }" @click="filter = 'bizError'">业务错 {{ counts.bizError }}</text>
                <text class="filter" :class="{ active: filter === 'unauth' }" @click="filter = 'unauth'">401 {{ counts.unauth }}</text>
                <text class="filter" :class="{ active: filter === 'fail' }" @click="filter = 'fail'">网络 {{ counts.fail }}</text>
            </view>
        </view>
        <view class="list">
            <view v-if="!filtered.length" class="empty">暂无调用记录</view>
            <view v-for="it in filtered" :key="it.id" class="row" :class="'st-' + it.status" @click="toggle(it)">
                <view class="row-line1">
                    <text class="method" :class="'m-' + it.method">{{ it.method }}</text>
                    <text class="url">{{ shortUrl(it.url) }}</text>
                </view>
                <view class="row-line2">
                    <text class="status" :class="'st-' + it.status">{{ statusText(it.status) }}</text>
                    <text class="cost">{{ it.costMs || 0 }}ms</text>
                    <text class="time">{{ formatTime(it.startedAt) }}</text>
                </view>
                <view v-if="it === expanded" class="row-line3">
                    <view class="kv">
                        <text class="k">URL</text>
                        <text class="v" user-select="true">{{ it.url }}</text>
                    </view>
                    <view class="kv">
                        <text class="k">Request Headers</text>
                        <text class="v" user-select="true">{{ it.request && it.request.header ? safeStringify(it.request.header) : '' }}</text>
                    </view>
                    <view v-if="it.request && it.request.data" class="kv">
                        <text class="k">Request Body</text>
                        <text class="v" user-select="true">{{ it.request.data }}</text>
                    </view>
                    <view v-if="it.response" class="kv">
                        <text class="k">Response ({{ it.response.statusCode || '' }})</text>
                        <text class="v" user-select="true">{{ it.response.body || it.response.error || '' }}</text>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import { getApiLogs, clearApiLogs, subscribeApiLog } from '@/utils/request.js'

export default {
    data() {
        return {
            logs: [],
            expanded: null,
            filter: 'all',
            unsubscribe: null
        }
    },
    computed: {
        filtered() {
            if (this.filter === 'all') return this.logs
            return this.logs.filter(it => it.status === this.filter)
        },
        counts() {
            const c = { all: this.logs.length, success: 0, bizError: 0, unauth: 0, fail: 0 }
            this.logs.forEach(it => { c[it.status] = (c[it.status] || 0) + 1 })
            return c
        }
    },
    onShow() {
        this.refresh()
        this.unsubscribe = subscribeApiLog(() => { this.logs = getApiLogs() })
    },
    onUnload() {
        if (typeof this.unsubscribe === 'function') this.unsubscribe()
    },
    methods: {
        refresh() { this.logs = getApiLogs() },
        clearAll() {
            clearApiLogs()
            this.logs = []
            this.expanded = null
        },
        toggle(it) { this.expanded = this.expanded === it ? null : it },
        shortUrl(u) {
            if (!u) return ''
            // 只保留路径，隐藏 host
            try {
                const i = u.indexOf('/api/')
                if (i >= 0) return u.substring(i)
            } catch (_) {}
            return u
        },
        statusText(s) {
            return ({
                pending: '请求中', success: '成功', bizError: '业务错',
                unauth: '401', fail: '网络错'
            })[s] || s
        },
        formatTime(ts) {
            if (!ts) return ''
            const d = new Date(ts)
            const pad = n => n < 10 ? '0' + n : '' + n
            return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${('' + d.getMilliseconds()).padStart(3, '0')}`
        },
        safeStringify(o) {
            try { return typeof o === 'string' ? o : JSON.stringify(o, null, 2) } catch (_) { return String(o) }
        },
        copyAll() {
            const text = this.filtered.map(it => {
                return [
                    `[${it.method}] ${it.url}`,
                    `Status: ${it.status} (${it.costMs}ms)`,
                    'Request:', this.safeStringify(it.request || {}),
                    'Response:', it.response ? this.safeStringify(it.response) : '',
                    '---'
                ].join('\n')
            }).join('\n')
            try {
                uni.setClipboardData({ data: text, success: () => uni.showToast({ title: '已复制', icon: 'success' }) })
            } catch (e) {
                uni.showToast({ title: '复制失败', icon: 'none' })
            }
        }
    }
}
</script>

<style scoped>
.page { background: #f5f6fa; min-height: 100vh; }
.header { background: #fff; padding: 16rpx 20rpx; position: sticky; top: 0; z-index: 10; border-bottom: 1rpx solid #eee; }
.actions { display: flex; gap: 12rpx; margin-bottom: 12rpx; }
.btn { font-size: 24rpx; padding: 6rpx 20rpx; border-radius: 24rpx; }
.btn.ghost { background: #f0f4ff; color: #0f62fe; border: 1rpx solid #c6d6ff; }
.filters { display: flex; flex-wrap: wrap; gap: 12rpx; }
.filter { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 20rpx; color: #666; background: #f0f0f0; }
.filter.active { background: #0f62fe; color: #fff; }
.list { padding: 12rpx 20rpx 80rpx; }
.row { background: #fff; border-radius: 12rpx; padding: 16rpx 20rpx; margin-bottom: 12rpx; }
.row-line1 { display: flex; align-items: center; gap: 12rpx; }
.row-line2 { display: flex; align-items: center; gap: 12rpx; margin-top: 6rpx; font-size: 22rpx; color: #999; }
.row-line3 { margin-top: 12rpx; padding-top: 12rpx; border-top: 1rpx dashed #eee; }
.kv { display: flex; gap: 12rpx; font-size: 22rpx; color: #333; line-height: 1.6; margin-bottom: 6rpx; }
.kv .k { width: 200rpx; color: #999; flex-shrink: 0; }
.kv .v { flex: 1; word-break: break-all; white-space: pre-wrap; }
.method { font-size: 20rpx; padding: 2rpx 12rpx; border-radius: 6rpx; font-weight: bold; }
.m-GET { background: #e6f7ff; color: #1890ff; }
.m-POST { background: #f6ffed; color: #52c41a; }
.m-PUT, .m-PATCH { background: #fff7e6; color: #fa8c16; }
.m-DELETE { background: #fff1f0; color: #f5222d; }
.url { font-size: 26rpx; color: #222; flex: 1; word-break: break-all; }
.status { padding: 2rpx 12rpx; border-radius: 6rpx; font-size: 22rpx; }
.st-pending { background: #f0f0f0; color: #666; }
.st-success { background: #e6f7ff; color: #1890ff; }
.st-bizError { background: #fff7e6; color: #fa8c16; }
.st-unauth { background: #fff1f0; color: #f5222d; }
.st-fail { background: #fff1f0; color: #f5222d; }
.cost { color: #888; }
.time { color: #aaa; margin-left: auto; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
