<template>
    <view class="page">
        <view class="form">
            <view class="avatar-row">
                <image class="avatar" src="/static/logo.png" mode="aspectFill" />
                <text class="change-avatar">修改头像</text>
            </view>
            <view class="form-item">
                <text class="label">昵称</text>
                <input class="input" v-model="form.nickname" placeholder="请输入昵称" />
            </view>
            <view class="form-item">
                <text class="label">手机号</text>
                <input class="input" v-model="form.phone" placeholder="请输入手机号" />
            </view>
        </view>
        <button class="btn" @click="save">保存</button>
    </view>
</template>

<script>
export default {
    data() {
        return { form: { nickname: '', phone: '' } }
    },
    onShow() { /* TODO: 加载用户信息 */ },
    methods: {
        save() {
            uni.showToast({ title: '保存成功', icon: 'success' })
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

.avatar-row {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
}

.change-avatar {
    margin-left: 24rpx;
    font-size: 28rpx;
    color: #0f62fe;
}

.form-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
}

.label {
    width: 120rpx;
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
