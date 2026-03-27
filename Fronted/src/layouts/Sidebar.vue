<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const collapsed = ref(false)
const route = useRoute()

const menuItems = [
    { path: '/dashboard', label: '仪表盘', icon: 'fa-th-large' },
    { path: '/product', label: '商品管理', icon: 'fa-box' },
    { path: '/customer', label: '客户管理', icon: 'fa-users' },
    { path: '/sale', label: '销售管理', icon: 'fa-shopping-cart' },
    { path: '/statistics', label: '数据统计', icon: 'fa-chart-bar' },
    { path: '/settings', label: '系统设置', icon: 'fa-cog' },
]

function toggleCollapse() {
    collapsed.value = !collapsed.value
}
</script>

<template>
    <aside class="h-screen bg-white shadow-md flex flex-col transition-all duration-200"
        :class="collapsed ? 'w-[72px]' : 'w-[260px]'">
        <!-- Logo -->
        <div class="h-14 flex items-center px-4 bg-gradient-to-br from-primary to-primary-dark">
            <h1 v-if="!collapsed" class="text-white font-bold text-lg truncate">
                销售管理系统
            </h1>
            <i v-else class="fas fa-chart-line text-white text-xl mx-auto" />
        </div>

        <!-- 菜单 -->
        <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-1">
            <router-link v-for="item in menuItems" :key="item.path" :to="item.path"
                class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors" :class="route.path === item.path || route.path.startsWith(item.path + '/')
                        ? 'bg-primary/10 text-primary font-medium'
                        : 'hover:bg-gray-100 text-gray-600'
                    ">
                <i class="fas" :class="item.icon" :style="{ width: '20px' }" />
                <span v-if="!collapsed" class="truncate">{{ item.label }}</span>
            </router-link>
        </nav>

        <!-- 折叠按钮 -->
        <div class="p-3 border-t border-gray-100">
            <button class="w-full flex items-center justify-center gap-2 px-3 py-2
               rounded-lg hover:bg-gray-100 text-gray-500 text-sm transition-colors" @click="toggleCollapse">
                <i class="fas" :class="collapsed ? 'fa-angle-right' : 'fa-angle-left'" />
                <span v-if="!collapsed">收起</span>
            </button>
        </div>
    </aside>
</template>
