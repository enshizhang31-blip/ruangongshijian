<template>
    <view class="page">
        <view class="form">
            <view class="form-item">
                <text class="label">收货人</text>
                <input class="input" v-model="form.name" placeholder="请输入收货人姓名" />
            </view>
            <view class="form-item">
                <text class="label">手机号</text>
                <input class="input" v-model="form.phone" type="number" placeholder="请输入手机号" />
            </view>
            <view class="form-item">
                <text class="label">地区</text>
                <input class="input" v-model="form.region" placeholder="省/市/区" />
            </view>
            <view class="form-item">
                <text class="label">详细地址</text>
                <input class="input" v-model="form.detail" placeholder="请输入详细地址" />
            </view>
        </view>
        <button class="btn" @click="save">保存</button>
    </view>
</template>

<script>
export default {
    data() {
        return { form: { name: '', phone: '', region: '', detail: '' } }
    },
    onLoad(options) {
        if (options.id) { /* TODO: 加载地址详情 */ }
    },
    methods: {
        save() {
            uni.showToast({ title: '保存成功', icon: 'success' })
            setTimeout(() => uni.navigateBack(), 1500)
        }
    }
}
</script>

<style scoped>
.page {
    padding: 20rpx;
}

.form {
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
}

.form-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.label {
    width: 140rpx;
    font-size: 28rpx;
    color: #666;
}

.input {
    flex: 1;
    font-size: 28rpx;
}

.btn {
    height: 80rpx;
    line-height: 80rpx;
    background: #0f62fe;
    color: #fff;
    border-radius: 40rpx;
    font-size: 32rpx;
    margin-top: 40rpx;
}
</style>
