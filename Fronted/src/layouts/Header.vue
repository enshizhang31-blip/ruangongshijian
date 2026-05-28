<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Avatar, Dropdown } from '@arco-design/web-vue'
import { IconMenuFold } from '@arco-design/web-vue/es/icon'
import { useAppStore } from '@/stores/app'
import { removeToken, removeUserInfo } from '@/utils/storage'

const router = useRouter()
const appStore = useAppStore()

const username = computed(() => appStore.userInfo?.realName || appStore.userInfo?.username || '未登录')

function handleMenuClick(key: string) {
    if (key === 'logout') {
        removeToken()
        removeUserInfo()
        appStore.logout()
        router.push('/login')
    }
}
</script>

<template>
    <header class="h-14 bg-white shadow-sm flex items-center justify-between px-4 lg:px-6">
        <div class="flex items-center gap-3">
            <!-- 移动端菜单按钮 -->
            <button @click="appStore.toggleMobileSidebar()"
                class="md:hidden flex items-center justify-center w-8 h-8 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors">
                <IconMenuFold class="w-5 h-5" />
            </button>
            <div class="text-sm text-gray-500">
                <slot name="breadcrumb" />
            </div>
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
                        <div @click="handleMenuClick('profile')"
                            class="px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                            个人设置
                        </div>
                        <div @click="handleMenuClick('logout')"
                            class="px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer">
                            退出登录
                        </div>
                    </div>
                </template>
            </Dropdown>
        </div>
    </header>
</template>
