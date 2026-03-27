<script setup lang="ts">
import { ref } from 'vue'

const username = ref('管理员')
</script>

<template>
    <header class="h-14 bg-white shadow-sm flex items-center justify-between px-6">
        <!-- 左侧：面包屑可在此扩展 -->
        <div class="text-sm text-gray-500">
            <slot name="breadcrumb" />
        </div>

        <!-- 右侧：用户信息 -->
        <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
                <div class="w-8 h-8 bg-gradient-to-br from-primary to-primary-dark rounded-full
                 flex items-center justify-center text-white text-sm font-medium">
                    {{ username.charAt(0) }}
                </div>
                <span class="text-sm text-gray-700">{{ username }}</span>
            </div>
            <button class="w-8 h-8 flex items-center justify-center rounded-full
               hover:bg-gray-100 text-gray-500 transition-colors" title="退出登录">
                <i class="fas fa-sign-out-alt" />
            </button>
        </div>
    </header>
</template>
