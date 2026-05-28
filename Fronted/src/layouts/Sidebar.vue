<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, MenuItem, SubMenu } from '@arco-design/web-vue'
import { useAppStore } from '@/stores/app'
import {
    IconApps,
    IconSafe,
    IconBulb,
    IconRobot,
    IconFire,
    IconLanguage,
    IconMenuFold,
    IconMenuUnfold,
} from '@arco-design/web-vue/es/icon'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const collapsed = ref(false)

const selectedKey = computed(() => {
    const p = route.path
    if (/^\/product\/\d+$/.test(p)) return '/product'
    return p
})
const defaultOpenKeys = computed(() => {
    const p = route.path
    if (p.startsWith('/product')) return ['/product']
    if (p.startsWith('/i18n')) return ['/i18n']
    return []
})

function go(path: string) {
    router.push(path)
    // 移动端点击菜单后自动关闭
    appStore.closeMobileSidebar()
}
</script>

<template>
    <!-- 移动端遮罩 -->
    <div v-if="appStore.mobileSidebarVisible" class="md:hidden fixed inset-0 z-40 bg-black/30 backdrop-blur-sm"
        @click="appStore.closeMobileSidebar()" />

    <!-- 侧边栏 -->
    <aside class="sidebar-container" :class="{
        collapsed,
        'mobile-open': appStore.mobileSidebarVisible,
    }" @click.stop>
        <div class="sidebar-logo">
            <span class="logo-text">销售管理</span>
            <span class="collapse-trigger hidden md:flex" @click="collapsed = !collapsed">
                <IconMenuFold v-if="!collapsed" />
                <IconMenuUnfold v-else />
            </span>
            <!-- 移动端关闭按钮 -->
            <button @click="appStore.closeMobileSidebar()"
                class="md:hidden flex items-center justify-center w-7 h-7 text-gray-500 hover:bg-gray-100 rounded-lg ml-auto">
                <IconMenuFold class="w-4 h-4" />
            </button>
        </div>
        <div class="menu-wrap">
            <Menu :selected-keys="[selectedKey]" :default-open-keys="defaultOpenKeys" :collapsed="collapsed"
                :style="{ width: collapsed ? '48px' : '200px' }">
                <MenuItem key="/dashboard" @click="go('/dashboard')">
                    <template #icon>
                        <IconApps />
                    </template>
                    首页
                </MenuItem>
                <SubMenu key="/product">
                    <template #icon>
                        <IconRobot />
                    </template>
                    <template #title>商品管理</template>
                    <MenuItem key="/product" @click="go('/product')">SPU管理</MenuItem>
                    <MenuItem key="/product/category" @click="go('/product/category')">商品分类</MenuItem>
                    <MenuItem key="/product/sku" @click="go('/product/sku')">SKU管理</MenuItem>
                    <MenuItem key="/product/spec" @click="go('/product/spec')">规格管理</MenuItem>
                    <MenuItem key="/sn" @click="go('/sn')">SN码管理</MenuItem>
                </SubMenu>
                <MenuItem key="/customer" @click="go('/customer')">
                    <template #icon>
                        <IconSafe />
                    </template>
                    客户管理
                </MenuItem>
                <MenuItem key="/sale" @click="go('/sale')">
                    <template #icon>
                        <IconFire />
                    </template>
                    销售订单
                </MenuItem>
                <MenuItem key="/admin/user" @click="go('/admin/user')">
                    <template #icon>
                        <IconSafe />
                    </template>
                    员工管理
                </MenuItem>
                <MenuItem key="/statistics" @click="go('/statistics')">
                    <template #icon>
                        <IconBulb />
                    </template>
                    数据统计
                </MenuItem>
                <MenuItem key="/settings" @click="go('/settings')">
                    <template #icon>
                        <IconApps />
                    </template>
                    系统设置
                </MenuItem>
            </Menu>
        </div>
    </aside>
</template>

<style scoped>
.sidebar-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #fff;
    border-right: 1px solid #e5e6eb;
    flex-shrink: 0;
    transition: width 0.2s, transform 0.3s;
    width: 200px;
}

.sidebar-container.collapsed {
    width: 48px;
}

/* 移动端：默认隐藏，使用抽屉式弹出 */
@media (max-width: 767px) {
    .sidebar-container {
        position: fixed;
        top: 0;
        left: 0;
        z-index: 50;
        height: 100vh;
        transform: translateX(-100%);
        width: 240px;
        box-shadow: 4px 0 12px rgba(0, 0, 0, 0.1);
    }

    .sidebar-container.mobile-open {
        transform: translateX(0);
    }
}

.sidebar-logo {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    border-bottom: 1px solid #e5e6eb;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
    flex-shrink: 0;
}

.logo-text {
    white-space: nowrap;
    overflow: hidden;
}

.collapse-trigger {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    cursor: pointer;
    color: #4e5969;
    border-radius: 4px;
    transition: all 0.2s;
    flex-shrink: 0;
}

.collapse-trigger:hover {
    background-color: #f2f3f5;
}

.menu-wrap {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
}
</style>
