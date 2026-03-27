<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
    ViewColumnsIcon,
    CubeIcon,
    UsersIcon,
    ShoppingCartIcon,
    Cog6ToothIcon,
} from '@heroicons/vue/24/outline'

const route = useRoute()

const selectedKey = route.path

const navItems = [
    { key: '/dashboard', label: '首页', icon: ViewColumnsIcon },
    { key: '/product', label: '商品', icon: CubeIcon },
    { key: '/customer', label: '客户', icon: UsersIcon },
    { key: '/sale', label: '订单', icon: ShoppingCartIcon },
    { key: '/settings', label: '设置', icon: Cog6ToothIcon },
]

function isActive(path: string) {
    return route.path === path || route.path.startsWith(path + '/')
}
</script>

<template>
    <nav class="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-200 safe-area-bottom">
        <div class="flex items-center justify-around h-14">
            <router-link v-for="item in navItems" :key="item.key" :to="item.key"
                class="flex flex-col items-center justify-center flex-1 py-1.5 gap-0.5 transition-colors" :class="route.path === item.key || route.path.startsWith(item.key + '/')
                    ? 'text-blue-600'
                    : 'text-gray-400'">
                <component :is="item.icon" class="w-5 h-5" />
                <span class="text-[10px] font-medium">{{ item.label }}</span>
            </router-link>
        </div>
    </nav>
</template>

<style scoped>
.safe-area-bottom {
    padding-bottom: env(safe-area-inset-bottom, 0);
}
</style>
