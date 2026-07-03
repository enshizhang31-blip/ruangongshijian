<template>
    <view class="page">
        <button class="btn-add" @click="goAdd">新增地址</button>
        <view class="address-item" v-for="a in addresses" :key="a.id">
            <view class="addr-info">
                <text class="addr-name">{{ a.receiverName }} {{ a.phone }}</text>
                <text class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</text>
                <text class="default-tag" v-if="a.isDefault === 1">默认</text>
            </view>
            <view class="addr-actions">
                <text class="action" @click="setDefault(a)">设为默认</text>
                <text class="action" @click="goEdit(a.id)">编辑</text>
                <text class="action danger" @click="deleteAddr(a)">删除</text>
            </view>
        </view>
        <view v-if="addresses.length === 0" class="empty">暂无地址，请新增</view>
    </view>
</template>

<script>
import { addressApi } from '@/api/index.js'

export default {
    data() {
        return { addresses: [] }
    },
    onShow() { this.loadAddresses() },
    methods: {
        async loadAddresses() {
            try {
                const res = await addressApi.list()
                this.addresses = (res?.list || res || [])
            } catch (e) {
                this.addresses = []
            }
        },
        goAdd() { uni.navigateTo({ url: '/pages/address/form/index' }) },
        goEdit(id) { uni.navigateTo({ url: '/pages/address/form/index?id=' + id }) },
        async setDefault(a) {
            try { await addressApi.setDefault(a.id) } catch (e) {}
            this.loadAddresses()
        },
        deleteAddr(a) {
            uni.showModal({
                content: '确定删除？',
                success: async r => {
                    if (!r.confirm) return
                    try { await addressApi.remove(a.id) } catch (e) {}
                    this.loadAddresses()
                }
            })
        }
    }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.btn-add { height: 72rpx; line-height: 72rpx; background: #0f62fe; color: #fff; border-radius: 36rpx; font-size: 28rpx; margin-bottom: 20rpx; }
.address-item { display: flex; justify-content: space-between; padding: 24rpx; background: #fff; border-radius: 12rpx; margin-bottom: 12rpx; }
.addr-info { flex: 1; }
.addr-name { font-size: 30rpx; color: #333; }
.addr-detail { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.default-tag { display: inline-block; margin-left: 12rpx; padding: 2rpx 12rpx; background: #fff7e6; color: #ff7d00; font-size: 22rpx; border-radius: 6rpx; }
.addr-actions { display: flex; flex-direction: column; gap: 12rpx; justify-content: center; }
.action { font-size: 24rpx; color: #0f62fe; }
.action.danger { color: #f53f2c; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
