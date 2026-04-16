<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, MenuItem, SubMenu } from '@arco-design/web-vue'
import {
    ViewColumnsIcon,
    CubeIcon,
    QueueListIcon,
    UsersIcon,
    ShoppingCartIcon,
    ChartBarIcon,
    UserGroupIcon,
    Cog6ToothIcon,
} from '@heroicons/vue/24/outline'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()

const selectedKey = computed(() => route.path.split('/').slice(0, 3).join('/'))
const currentPath = computed(() => route.path)

const menuItems = [
    { key: '/dashboard', label: '首页', icon: ViewColumnsIcon },
    {
        key: '/product',
        label: '商品管理',
        icon: CubeIcon,
        children: [
            { key: '/product', label: '商品列表' },
            { key: '/product/category', label: '商品分类' },
            { key: '/product/sku', label: 'SKU管理' },
            { key: '/product/spec', label: '规格管理' },
        ]
    },
    { key: '/sn', label: 'SN码管理', icon: QueueListIcon },
    { key: '/customer', label: '客户管理', icon: UsersIcon },
    { key: '/sale', label: '销售订单', icon: ShoppingCartIcon },
    { key: '/admin/user', label: '员工管理', icon: UserGroupIcon },
    { key: '/statistics', label: '数据统计', icon: ChartBarIcon },
    { key: '/settings', label: '系统设置', icon: Cog6ToothIcon },
]

function isActive(key: string) {
    if (key === '/product') return currentPath.value === '/product' || currentPath.value.startsWith('/product/')
    return currentPath.value.startsWith(key)
}
</script>

<template>
    <aside class="flex h-full flex-col bg-white border-r border-gray-200 shrink-0">
        <!-- Logo Area -->
        <div class="h-14 px-4 flex items-center border-b border-gray-200 cursor-pointer"
            @click="appStore.toggleSidebar">
            <div class="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center mr-3">
                <span class="text-white font-bold text-sm">SM</span>
            </div>
            <span class="text-gray-800 font-semibold text-sm">销售管理</span>
        </div>

        <!-- Navigation -->
        <div class="flex-1 overflow-y-auto py-2">
            <Menu :default-selected-keys="[selectedKey]" class="border-0 bg-transparent">
                <template v-for="item in menuItems" :key="item.key">
                    <SubMenu v-if="item.children" :key="item.key">
                        <template #title>
                            <div class="flex items-center gap-3 px-3 py-2.5 rounded-lg" :class="isActive(item.key) ? 'bg-blue-50 text-blue-600' : 'text-gray-600'">
                                <component :is="item.icon" class="w-5 h-5" />
                                <span class="text-sm font-medium">{{ item.label }}</span>
                            </div>
                        </template>
                        <MenuItem v-for="child in item.children" :key="child.key">
                            <router-link :to="child.key" class="flex items-center gap-3 px-3 py-2 text-sm rounded-lg transition-colors"
                                :class="currentPath === child.key ? 'text-blue-600 bg-blue-50' : 'text-gray-600 hover:bg-gray-50'">
                                {{ child.label }}
                            </router-link>
                        </MenuItem>
                    </SubMenu>
                    <MenuItem v-else :key="item.key">
                        <router-link :to="item.key" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors"
                            :class="isActive(item.key) ? 'bg-blue-50 text-blue-600' : 'text-gray-600 hover:bg-gray-50'">
                            <component :is="item.icon" class="w-5 h-5" />
                            <span class="text-sm font-medium">{{ item.label }}</span>
                        </router-link>
                    </MenuItem>
                </template>
            </Menu>
        </div>
    </aside>
</template>
