<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, MenuItem, SubMenu } from '@arco-design/web-vue'
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
const collapsed = ref(false)

const selectedKey = computed(() => {
    const p = route.path
    // 动态路由：SPU 详情页高亮 SPU管理
    if (/^\/product\/\d+$/.test(p)) return '/product'
    // 动态路由：SN码页高亮 SN码(按SKU)
    if (/^\/sn\/sku\/\d+$/.test(p)) return '/sn/sku'
    return p
})
const defaultOpenKeys = computed(() => {
    const p = route.path
    if (p.startsWith('/product')) return ['/product']
    if (p.startsWith('/sn')) return ['/sn']
    if (p.startsWith('/i18n')) return ['/i18n']
    return []
})

function go(path: string) {
    router.push(path)
}
</script>

<template>
    <aside class="sidebar-container" :class="{ collapsed }">
        <div class="sidebar-logo">
            <span class="logo-text">销售管理</span>
            <span class="collapse-trigger" @click="collapsed = !collapsed">
                <IconMenuFold v-if="!collapsed" />
                <IconMenuUnfold v-else />
            </span>
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
                </SubMenu>

                <SubMenu key="/i18n">
                    <template #icon>
                        <IconLanguage />
                    </template>
                    <template #title>多语言</template>
                    <MenuItem key="/i18n/status" @click="go('/i18n/status')">翻译状态</MenuItem>
                    <MenuItem key="/i18n" @click="go('/i18n')">翻译编辑</MenuItem>
                </SubMenu>

                <SubMenu key="/sn">
                    <template #icon>
                        <IconBulb />
                    </template>
                    <template #title>SN码管理</template>
                    <MenuItem key="/sn" @click="go('/sn')">SN码列表</MenuItem>
                    <MenuItem key="/sn/sku" @click="go('/sn/sku/0')">SN码(按SKU)</MenuItem>
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
    transition: width 0.2s;
    width: 200px;
}

.sidebar-container.collapsed {
    width: 48px;
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
