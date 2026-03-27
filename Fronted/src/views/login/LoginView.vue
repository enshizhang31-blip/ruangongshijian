<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, Button, Message } from '@arco-design/web-vue'
import { setToken } from '@/utils/storage'

const router = useRouter()
const loading = ref(false)
const form = ref({
    username: '',
    password: '',
})

async function handleLogin() {
    if (!form.value.username || !form.value.password) {
        Message.warning('请输入用户名和密码')
        return
    }
    loading.value = true
    try {
        setToken('mock-token')
        router.push('/dashboard')
    } catch {
        Message.error('登录失败')
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="min-h-screen bg-blue-500 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-8">
            <div class="text-center mb-8">
                <div class="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <span class="text-white font-bold text-2xl">SM</span>
                </div>
                <h1 class="text-2xl font-bold text-gray-800">销售管理系统</h1>
                <p class="text-gray-500 mt-2">请登录您的账号</p>
            </div>

            <Form :model="form" @submit="handleLogin">
                <FormItem>
                    <Input v-model="form.username" placeholder="用户名" size="large" allow-clear />
                </FormItem>
                <FormItem>
                    <Input v-model="form.password" type="password" placeholder="密码" size="large" allow-clear />
                </FormItem>
                <FormItem>
                    <Button type="primary" html-type="submit" long size="large" :loading="loading">
                        {{ loading ? '登录中...' : '登录' }}
                    </Button>
                </FormItem>
            </Form>
        </div>
    </div>
</template>
