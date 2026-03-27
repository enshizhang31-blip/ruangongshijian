<script setup lang="ts">
import { ref } from 'vue'
import { Avatar, Dropdown } from '@arco-design/web-vue'
import { ArrowRightStartOnRectangleIcon } from '@heroicons/vue/24/outline'

const username = ref('管理员')

const userMenu = [
    { key: 'profile', label: '个人设置' },
    { key: 'logout', label: '退出登录' },
]
</script>

<template>
    <header class="h-14 bg-white shadow-sm flex items-center justify-between px-4 lg:px-6">
        <div class="text-sm text-gray-500">
            <slot name="breadcrumb" />
        </div>

        <div class="flex items-center gap-4">
            <Dropdown trigger="click">
                <div
                    class="flex items-center gap-3 cursor-pointer hover:bg-gray-50 px-2 py-1.5 rounded-lg transition-colors">
                    <Avatar :size="32" :style="{ backgroundColor: '#0f62fe' }">
                        {{ username.charAt(0) }}
                    </Avatar>
                    <span class="text-sm text-gray-700 hidden sm:block">{{ username }}</span>
                </div>
                <template #content>
                    <div class="py-1">
                        <div v-for="item in userMenu" :key="item.key"
                            class="px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                            {{ item.label }}
                        </div>
                    </div>
                </template>
            </Dropdown>
        </div>
    </header>
</template>
