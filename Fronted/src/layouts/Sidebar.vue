<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Menu, MenuItem, SubMenu } from '@arco-design/web-vue'
import {
    IconApps,
    IconSafe,
    IconBulb,
    IconRobot,
    IconFire,
    IconLanguage,
} from '@arco-design/web-vue/es/icon'

const route = useRoute()

const selectedKey = computed(() => {
  const p = route.path
  if (p.startsWith('/sn/sku/')) return '/sn/sku'
  return p
})
const defaultOpenKeys = computed(() => {
  const p = route.path
  if (p.startsWith('/product')) return ['/product']
  if (p.startsWith('/sn')) return ['/sn']
  if (p.startsWith('/i18n')) return ['/i18n']
  return []
})
</script>

<template>
    <aside class="sidebar-container">
        <div class="sidebar-logo">销售管理</div>
        <div class="menu-demo-round">
            <Menu :selected-keys="[selectedKey]" :default-open-keys="defaultOpenKeys" :has-collapse-button="true"
                mode="pop" :style="{ width: '200px' }">
                <MenuItem key="/dashboard">
                <router-link to="/dashboard" class="menu-link">
                    <IconApps />
                    首页
                </router-link>
                </MenuItem>

                <SubMenu key="/product">
                    <template #title>
                        <IconRobot />
                        商品管理
                    </template>
                    <MenuItem key="/product">
                    <router-link to="/product" class="menu-link-inline">商品列表</router-link>
                    </MenuItem>
                    <MenuItem key="/product/category">
                    <router-link to="/product/category" class="menu-link-inline">商品分类</router-link>
                    </MenuItem>
                    <MenuItem key="/product/sku">
                    <router-link to="/product/sku" class="menu-link-inline">SKU管理</router-link>
                    </MenuItem>
                    <MenuItem key="/product/spec">
                    <router-link to="/product/spec" class="menu-link-inline">规格管理</router-link>
                    </MenuItem>
                </SubMenu>

                <SubMenu key="/i18n">
                    <template #title>
                        <IconLanguage />
                        多语言
                    </template>
                    <MenuItem key="/i18n/status">
                    <router-link to="/i18n/status" class="menu-link-inline">翻译状态</router-link>
                    </MenuItem>
                    <MenuItem key="/i18n">
                    <router-link to="/i18n" class="menu-link-inline">翻译编辑</router-link>
                    </MenuItem>
                </SubMenu>

                <SubMenu key="/sn">
                    <template #title>
                        <IconBulb />
                        SN码管理
                    </template>
                    <MenuItem key="/sn">
                    <router-link to="/sn" class="menu-link-inline">SN码列表</router-link>
                    </MenuItem>
                    <MenuItem key="/sn/sku">
                    <router-link to="/sn/sku/0" class="menu-link-inline">SN码(按SKU)</router-link>
                    </MenuItem>
                </SubMenu>

                <MenuItem key="/customer">
                <router-link to="/customer" class="menu-link">
                    <IconSafe />
                    客户管理
                </router-link>
                </MenuItem>

                <MenuItem key="/sale">
                <router-link to="/sale" class="menu-link">
                    <IconFire />
                    销售订单
                </router-link>
                </MenuItem>

                <MenuItem key="/admin/user">
                <router-link to="/admin/user" class="menu-link">
                    <IconSafe />
                    员工管理
                </router-link>
                </MenuItem>

                <MenuItem key="/statistics">
                <router-link to="/statistics" class="menu-link">
                    <IconBulb />
                    数据统计
                </router-link>
                </MenuItem>

                <MenuItem key="/settings">
                <router-link to="/settings" class="menu-link">
                    <IconApps />
                    系统设置
                </router-link>
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
}

.sidebar-logo {
    height: 56px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-bottom: 1px solid #e5e6eb;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
}

.menu-demo-round {
    height: calc(100% - 56px);
    padding-top: 8px;
    overflow-y: auto;
}

.menu-link {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    color: inherit;
    text-decoration: none;
}

.menu-link-inline {
    display: inline-block;
    width: 100%;
    color: inherit;
    text-decoration: none;
}
</style>
