<template>
    <view class="page">
        <button class="btn-add" @click="goAdd">新增地址</button>
        <view class="address-item" v-for="a in addresses" :key="a.id">
            <view class="addr-info">
                <text class="addr-name">{{ a.name }} {{ a.phone }}</text>
                <text class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</text>
            </view>
            <view class="addr-actions">
                <text class="action" @click="goEdit(a.id)">编辑</text>
                <text class="action" @click="deleteAddr(a.id)">删除</text>
            </view>
        </view>
        <view v-if="addresses.length === 0" class="empty">暂无地址</view>
    </view>
</template>

<script>
export default {
    data() {
        return { addresses: [] }
    },
    onShow() { this.loadAddresses() },
    methods: {
        loadAddresses() { /* TODO: API */ },
        goAdd() { uni.navigateTo({ url: '/pages/address/form' }) },
        goEdit(id) { uni.navigateTo({ url: '/pages/address/form?id=' + id }) },
        deleteAddr(id) { uni.showModal({ content: '确定删除？', success: () => { /* TODO */ } }) }
    }
}
</script>

<style scoped>
.page {
    padding: 20rpx;
}

.btn-add {
    height: 72rpx;
    line-height: 72rpx;
    background: #0f62fe;
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;
    margin-bottom: 20rpx;
}

.address-item {
    display: flex;
    justify-content: space-between;
    padding: 24rpx;
    background: #fff;
    border-radius: 12rpx;
    margin-bottom: 12rpx;
}

.addr-info {
    flex: 1;
}

.addr-name {
    font-size: 30rpx;
    color: #333;
}

.addr-detail {
    font-size: 24rpx;
    color: #999;
    margin-top: 8rpx;
}

.addr-actions {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
}

.action {
    font-size: 26rpx;
    color: #0f62fe;
}

.empty {
    text-align: center;
    color: #999;
    padding: 80rpx 0;
}
</style>
