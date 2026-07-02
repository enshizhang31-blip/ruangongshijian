<template>
    <view class="page">
        <view class="form">
            <view class="form-item">
                <text class="label">收货人</text>
                <input class="input" v-model="form.receiverName" placeholder="请输入收货人姓名" />
            </view>
            <view class="form-item">
                <text class="label">手机号</text>
                <input class="input" v-model="form.phone" type="number" placeholder="请输入手机号" maxlength="11" />
            </view>
            <view class="form-item">
                <text class="label">省</text>
                <input class="input" v-model="form.province" placeholder="省" />
            </view>
            <view class="form-item">
                <text class="label">市</text>
                <input class="input" v-model="form.city" placeholder="市" />
            </view>
            <view class="form-item">
                <text class="label">区</text>
                <input class="input" v-model="form.district" placeholder="区/县" />
            </view>
            <view class="form-item">
                <text class="label">详细地址</text>
                <input class="input" v-model="form.detail" placeholder="街道、楼栋、门牌号" />
            </view>
            <view class="form-item switch-item">
                <text class="label">设为默认</text>
                <switch :checked="form.isDefault === 1" @change="e => form.isDefault = e.detail.value ? 1 : 0" />
            </view>
        </view>
        <button class="btn" @click="save" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
    </view>
</template>

<script>
import { addressApi } from '@/api/index.js'

export default {
    data() {
        return {
            form: {
                id: null,
                receiverName: '',
                phone: '',
                province: '',
                city: '',
                district: '',
                detail: '',
                isDefault: 0
            },
            saving: false
        }
    },
    onLoad() {},
    onShow() {},
    methods: {
        async save() {
            const f = this.form
            if (!f.receiverName || !f.phone || !f.province || !f.detail) {
                uni.showToast({ title: '请完整填写地址', icon: 'none' })
                return
            }
            this.saving = true
            try {
                if (f.id) {
                    await addressApi.update(f.id, {
                        receiverName: f.receiverName,
                        phone: f.phone,
                        province: f.province,
                        city: f.city,
                        district: f.district,
                        detail: f.detail,
                        isDefault: f.isDefault
                    })
                } else {
                    await addressApi.create({
                        receiverName: f.receiverName,
                        phone: f.phone,
                        province: f.province,
                        city: f.city,
                        district: f.district,
                        detail: f.detail,
                        isDefault: f.isDefault,
                        status: 1
                    })
                }
                uni.showToast({ title: '保存成功', icon: 'success' })
                setTimeout(() => uni.navigateBack(), 600)
            } catch (e) {
                uni.showToast({ title: e.message || '保存失败', icon: 'none' })
            } finally { this.saving = false }
        }
    }
}
</script>

<style scoped>
.page { padding: 20rpx; }
.form { background: #fff; border-radius: 12rpx; padding: 20rpx; }
.form-item { display: flex; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.form-item.switch-item { justify-content: space-between; }
.label { width: 140rpx; font-size: 28rpx; color: #666; }
.input { flex: 1; font-size: 28rpx; }
.btn { height: 80rpx; line-height: 80rpx; background: #0f62fe; color: #fff; border-radius: 40rpx; font-size: 32rpx; margin-top: 40rpx; }
</style>
