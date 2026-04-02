<script setup lang="ts">
import { ref } from 'vue'
import { Card, Tabs, TabPane, Form, FormItem, Input, Button, Switch, Space, Message } from '@arco-design/web-vue'

const activeTab = ref('profile')

// 基础设置表单
const profileForm = ref({
    username: 'admin',
    realName: '系统管理员',
    phone: '',
    email: '',
})

// 系统设置表单
const systemForm = ref({
    productAutoAudit: true,
    orderAutoConfirm: false,
    lowStockWarning: 10,
})

function handleSaveProfile() {
    Message.success('保存成功')
}

function handleSaveSystem() {
    Message.success('保存成功')
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="mb-6">
            <h1 class="text-xl lg:text-2xl font-bold text-gray-800">系统设置</h1>
            <p class="text-sm text-gray-500 mt-1">管理系统配置</p>
        </div>

        <Card>
            <Tabs v-model:active-key="activeTab">
                <TabPane key="profile">
                    <template #title>个人信息</template>
                    <div class="max-w-xl">
                        <Form :model="profileForm" layout="vertical">
                            <FormItem label="用户名">
                                <Input v-model="profileForm.username" disabled />
                            </FormItem>
                            <FormItem label="姓名">
                                <Input v-model="profileForm.realName" />
                            </FormItem>
                            <FormItem label="手机号">
                                <Input v-model="profileForm.phone" placeholder="请输入手机号" />
                            </FormItem>
                            <FormItem label="邮箱">
                                <Input v-model="profileForm.email" placeholder="请输入邮箱" />
                            </FormItem>
                            <FormItem>
                                <Button type="primary" @click="handleSaveProfile">保存</Button>
                            </FormItem>
                        </Form>
                    </div>
                </TabPane>

                <TabPane key="system">
                    <template #title>系统配置</template>
                    <div class="max-w-xl">
                        <Form :model="systemForm" layout="vertical">
                            <FormItem label="商品自动审核">
                                <Switch v-model="systemForm.productAutoAudit" />
                            </FormItem>
                            <FormItem label="订单自动确认">
                                <Switch v-model="systemForm.orderAutoConfirm" />
                            </FormItem>
                            <FormItem label="库存预警值">
                                <InputNumber v-model="systemForm.lowStockWarning" :min="1" :max="100" />
                            </FormItem>
                            <FormItem>
                                <Button type="primary" @click="handleSaveSystem">保存</Button>
                            </FormItem>
                        </Form>
                    </div>
                </TabPane>
            </Tabs>
        </Card>
    </div>
</template>
