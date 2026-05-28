<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { adminApi, departmentApi, roleApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card, Modal, Form, FormItem, Select, Popconfirm, Message, Spin, Empty } from '@arco-design/web-vue'
import type { AdminUser, Department, Role } from '@/types'
import { PlusIcon, ShieldCheckIcon, ChevronRightIcon, ChevronLeftIcon, ArrowsRightLeftIcon } from '@heroicons/vue/24/outline'
import draggable from 'vuedraggable'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(adminApi.list)
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const departments = ref<Department[]>([])
const roles = ref<Role[]>([])
const loadingDepts = ref(false)
const loadingRoles = ref(false)

// 权限管理
const showPermModal = ref(false)
const permEditingId = ref<number>()
const permLoading = ref(false)
const userPermissions = ref<string[]>([])
const userRoutes = ref<string[]>([])
const allPermissions = [
    'spu:view', 'spu:add', 'spu:edit', 'spu:delete', 'spu:import', 'spu:export', 'spu:status',
    'sku:view', 'sku:add', 'sku:edit', 'sku:delete',
    'category:view', 'category:add', 'category:edit', 'category:delete',
    'spec:view', 'spec:add', 'spec:edit', 'spec:delete',
    'sn:view', 'sn:add', 'sn:import', 'sn:export', 'sn:generate', 'sn:query', 'sn:status',
    'customer:view', 'customer:detail', 'customer:edit', 'customer:balance', 'customer:points', 'customer:disable',
    'order:view', 'order:detail', 'order:process', 'order:refund',
    'statistics:view',
    'system:user', 'system:role', 'system:menu', 'system:log',
]
const allRoutes = [
    '/dashboard', '/product', '/product/list',
    '/sn', '/sn/list',
    '/order', '/order/list',
    '/customer', '/customer/list',
    '/statistics',
    '/system', '/system/user', '/system/role',
]

// 拖拽专用：可用列表（排除已选）
const availablePermissions = ref<string[]>([...allPermissions])
const availableRoutes = ref<string[]>([...allRoutes])

const form = reactive<{
    id?: number
    username: string
    password: string
    realName: string
    phone: string
    email: string
    roleId: number | undefined
    departmentId: number | undefined
    status: number
}>({
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    roleId: undefined,
    departmentId: undefined,
    status: 1,
})

const columns = [
    { title: '用户名', dataIndex: 'username', width: 120 },
    { title: '姓名', dataIndex: 'realName', width: 100 },
    { title: '手机号', dataIndex: 'phone', width: 130 },
    {
        title: '状态',
        dataIndex: 'status',
        render: (status: number) => h(Tag, { color: status === 1 ? 'green' : 'red' }, () => status === 1 ? '正常' : '禁用')
    },
    { title: '最后登录', dataIndex: 'lastLoginAt', render: (t: string) => t ? formatDate(t) : '-' },
    { title: '创建时间', dataIndex: 'createdAt', render: (t: string) => t ? formatDate(t) : '-' },
    { title: '操作', slotName: 'actions', width: 300, fixed: 'right' },
]

onMounted(async () => {
    load()
    fetchDepartments()
    fetchRoles()
})

async function fetchDepartments() {
    loadingDepts.value = true
    try {
        departments.value = await departmentApi.list()
    } catch (e: any) {
        console.error('获取部门失败:', e)
        Message.error('获取部门列表失败')
    } finally {
        loadingDepts.value = false
    }
}

async function fetchRoles() {
    loadingRoles.value = true
    try {
        roles.value = await roleApi.all()
    } catch (e: any) {
        console.error('获取角色失败:', e)
        Message.error('获取角色列表失败')
    } finally {
        loadingRoles.value = false
    }
}

function handleSearch() {
    setKeyword(keyword.value)
}

function handleReset() {
    keyword.value = ''
    setKeyword('')
}

function handleAdd() {
    isEdit.value = false
    editingId.value = undefined
    Object.assign(form, {
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        roleId: undefined,
        departmentId: undefined,
        status: 1,
    })
    showModal.value = true
}

function handleEdit(record: AdminUser) {
    isEdit.value = true
    editingId.value = record.id
    Object.assign(form, { ...record })
    showModal.value = true
}

async function handleSubmit() {
    if (!form.username) {
        Message.warning('请填写用户名')
        return false
    }
    if (!isEdit.value && !form.password) {
        Message.warning('请填写密码')
        return false
    }
    if (!form.realName) {
        Message.warning('请填写姓名')
        return false
    }
    try {
        if (isEdit.value && editingId.value) {
            // 编辑时只提交允许更新的字段
            const updateData = {
                id: editingId.value,
                realName: form.realName,
                phone: form.phone,
                email: form.email,
                departmentId: form.departmentId,
                status: form.status,
            }
            await adminApi.update(editingId.value, updateData as any)
            Message.success('更新成功')
        } else {
            // 新增时提交完整数据
            await adminApi.create(form as any)
            Message.success('创建成功')
        }
        showModal.value = false
        load()
        return true
    } catch (e: any) {
        Message.error(e?.message || '操作失败')
        return false
    }
}

async function handleDelete(id: number) {
    await adminApi.delete(id)
    Message.success('删除成功')
    load()
}

async function handleResetPassword(id: number) {
    await adminApi.resetPassword(id)
    Message.success('密码已重置为 123456')
}

async function handleOpenPermModal(record: AdminUser) {
    permEditingId.value = record.id
    permLoading.value = true
    showPermModal.value = true
    try {
        const res = await adminApi.getPermissions(record.id!)
        const perms = res.permissions || []
        const routes = res.routes || []
        userPermissions.value = perms
        userRoutes.value = routes
        // 初始化可用列表：排除已选
        availablePermissions.value = allPermissions.filter(p => !perms.includes(p))
        availableRoutes.value = allRoutes.filter(r => !routes.includes(r))
    } catch (e: any) {
        Message.error('获取权限失败')
    } finally {
        permLoading.value = false
    }
}

async function handleSavePerm() {
    if (!permEditingId.value) return
    try {
        await adminApi.updatePermissions(permEditingId.value, {
            permissions: userPermissions.value,
            routes: userRoutes.value,
        })
        Message.success('权限更新成功')
        showPermModal.value = false
    } catch (e: any) {
        Message.error(e?.message || '权限更新失败')
        return false
    }
}

function onPermissionAdd(evt: any) {
    // 去重：如果已有则移除
    const val = evt.item._underlying_vm_
    if (val && userPermissions.value.includes(val)) {
        userPermissions.value = userPermissions.value.filter(p => p !== val)
    }
    // 从可用列表移除
    availablePermissions.value = availablePermissions.value.filter(p => p !== val)
}

function onRouteAdd(evt: any) {
    const val = evt.item._underlying_vm_
    if (val && userRoutes.value.includes(val)) {
        userRoutes.value = userRoutes.value.filter(r => r !== val)
    }
    availableRoutes.value = availableRoutes.value.filter(r => r !== val)
}

// ---- 拖拽辅助函数（按钮备选操作） ----
function addPermission(p: string) {
    if (!userPermissions.value.includes(p)) {
        userPermissions.value.push(p)
        availablePermissions.value = availablePermissions.value.filter(x => x !== p)
    }
}

function removePermission(p: string) {
    userPermissions.value = userPermissions.value.filter(x => x !== p)
    if (allPermissions.includes(p) && !availablePermissions.value.includes(p)) {
        availablePermissions.value.push(p)
    }
}

function addRoute(r: string) {
    if (!userRoutes.value.includes(r)) {
        userRoutes.value.push(r)
        availableRoutes.value = availableRoutes.value.filter(x => x !== r)
    }
}

function removeRoute(r: string) {
    userRoutes.value = userRoutes.value.filter(x => x !== r)
    if (allRoutes.includes(r) && !availableRoutes.value.includes(r)) {
        availableRoutes.value.push(r)
    }
}

function selectAllPermissions() {
    userPermissions.value = [...allPermissions]
    availablePermissions.value = []
}

function clearAllPermissions() {
    availablePermissions.value = [...allPermissions]
    userPermissions.value = []
}

function selectAllRoutes() {
    userRoutes.value = [...allRoutes]
    availableRoutes.value = []
}

function clearAllRoutes() {
    availableRoutes.value = [...allRoutes]
    userRoutes.value = []
}

function togglePermission(p: string) {
    if (userPermissions.value.includes(p)) {
        removePermission(p)
    } else {
        addPermission(p)
    }
}

function toggleRoute(r: string) {
    if (userRoutes.value.includes(r)) {
        removeRoute(r)
    } else {
        addRoute(r)
    }
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">员工管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理系统员工账号</p>
            </div>
            <Button type="primary" @click="handleAdd">
                <template #icon>
                    <PlusIcon class="w-4 h-4" />
                </template>
                新增员工
            </Button>
        </div>

        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索用户名或姓名..." class="w-64!" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Button type="primary" @click="handleSearch">搜索</Button>
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <Card>
            <!-- 错误状态 -->
            <div v-if="error" class="text-center py-8">
                <div class="text-red-500 mb-2">加载失败: {{ error.message }}</div>
                <Button type="primary" size="small" @click="load">重试</Button>
            </div>

            <!-- 空状态 -->
            <div v-else-if="!loading && list.length === 0" class="text-center py-8">
                <Empty description="暂无数据" />
            </div>

            <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1000 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleEdit(record)">编辑</Button>
                        <Button type="text" size="small" @click="handleOpenPermModal(record)">权限</Button>
                        <Popconfirm title="确定重置该员工密码？" @ok="handleResetPassword(record.id)">
                            <Button type="text" size="small">重置密码</Button>
                        </Popconfirm>
                        <Popconfirm title="确定删除该员工？" @ok="handleDelete(record.id)">
                            <Button type="text" status="danger" size="small">删除</Button>
                        </Popconfirm>
                    </Space>
                </template>
            </Table>

            <div class="flex justify-end mt-4">
                <Space direction="horizontal">
                    <span class="text-sm text-gray-500">共 {{ total }} 条</span>
                    <Button :disabled="(query.page || 1) <= 1" @click="setPage((query.page || 1) - 1)">上一页</Button>
                    <span class="text-sm py-2">第 {{ query.page || 1 }} / {{ Math.ceil(total / (query.pageSize || 10)) ||
                        1 }}
                        页</span>
                    <Button :disabled="(query.page || 1) >= Math.ceil(total / (query.pageSize || 10))"
                        @click="setPage((query.page || 1) + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <Modal v-model:visible="showModal" :title="isEdit ? '编辑员工' : '新增员工'" :on-before-ok="handleSubmit" :width="500">
        <Form :model="form" layout="vertical">
            <FormItem label="用户名" required>
                <Input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
            </FormItem>
            <FormItem :label="isEdit ? '密码（留空则不修改）' : '密码'">
                <Input v-model="form.password" type="password" placeholder="请输入密码" />
            </FormItem>
            <FormItem label="姓名" required>
                <Input v-model="form.realName" placeholder="请输入姓名" />
            </FormItem>
            <FormItem v-if="!isEdit" label="角色" required>
                <Select v-model="form.roleId" placeholder="请选择角色" class="w-full" :loading="loadingRoles">
                    <Select.Option v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</Select.Option>
                </Select>
            </FormItem>
            <FormItem label="手机号">
                <Input v-model="form.phone" placeholder="请输入手机号" />
            </FormItem>
            <FormItem label="邮箱">
                <Input v-model="form.email" placeholder="请输入邮箱" />
            </FormItem>
            <FormItem label="部门">
                <Select v-model="form.departmentId" placeholder="请选择部门" class="w-full" :loading="loadingDepts">
                    <Select.Option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}
                    </Select.Option>
                </Select>
            </FormItem>
            <FormItem label="状态">
                <Select v-model="form.status" class="w-full">
                    <Select.Option :value="1">正常</Select.Option>
                    <Select.Option :value="0">禁用</Select.Option>
                </Select>
            </FormItem>
        </Form>
    </Modal>

    <!-- 权限编辑弹窗 -->
    <Modal v-model:visible="showPermModal" title="编辑权限" :on-before-ok="handleSavePerm" :width="820"
        :loading="permLoading">
        <div class="flex flex-col gap-6 max-h-[70vh] overflow-y-auto pr-2">

            <!-- 操作权限 -->
            <section>
                <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-2 font-semibold text-gray-700">
                        <ShieldCheckIcon class="w-5 h-5 text-blue-500" />
                        操作权限
                    </div>
                    <Space>
                        <Button size="mini" type="outline" @click="selectAllPermissions">全选</Button>
                        <Button size="mini" type="outline" status="danger" @click="clearAllPermissions">清空</Button>
                    </Space>
                </div>
                <div class="grid grid-cols-[1fr_40px_1fr] gap-3 items-stretch">
                    <!-- 可用权限 -->
                    <div
                        class="border border-dashed border-gray-300 rounded-xl p-3 bg-gray-50 flex flex-col min-h-[220px]">
                        <div class="text-xs font-medium text-gray-400 mb-2 uppercase tracking-wide">
                            可用权限 ({{ availablePermissions.length }})
                        </div>
                        <draggable v-model="availablePermissions" :group="{ name: 'perm', pull: 'clone', put: false }"
                            :sort="false" item-key="(item: string) => item" class="flex-1 overflow-y-auto space-y-1">
                            <template #item="{ element }">
                                <div class="flex items-center justify-between px-3 py-1.5 bg-white border border-gray-100 rounded-lg text-sm cursor-grab hover:border-blue-400 hover:text-blue-600 hover:shadow-sm transition-all group"
                                    @click="addPermission(element)">
                                    <span class="font-mono text-xs">{{ element }}</span>
                                    <ChevronRightIcon
                                        class="w-4 h-4 text-gray-300 group-hover:text-blue-500 flex-shrink-0" />
                                </div>
                            </template>
                        </draggable>
                    </div>
                    <!-- 箭头指示 -->
                    <div class="hidden md:flex flex-col items-center justify-center text-gray-300 py-4">
                        <ArrowsRightLeftIcon class="w-5 h-5" />
                    </div>
                    <!-- 已选权限 -->
                    <div class="border border-blue-200 rounded-xl p-3 bg-blue-50/40 flex flex-col min-h-[220px]">
                        <div class="text-xs font-medium text-blue-500 mb-2 uppercase tracking-wide">
                            已选权限 ({{ userPermissions.length }})
                        </div>
                        <draggable v-model="userPermissions" group="perm" item-key="(item: string) => item"
                            class="flex-1 overflow-y-auto space-y-1" @add="onPermissionAdd">
                            <template #item="{ element }">
                                <div class="flex items-center justify-between px-3 py-1.5 bg-white border border-blue-100 rounded-lg text-sm cursor-grab hover:border-red-400 hover:text-red-500 hover:shadow-sm transition-all group"
                                    @click="removePermission(element)">
                                    <span class="font-mono text-xs text-blue-700">{{ element }}</span>
                                    <ChevronLeftIcon
                                        class="w-4 h-4 text-gray-300 group-hover:text-red-400 flex-shrink-0" />
                                </div>
                            </template>
                        </draggable>
                    </div>
                </div>
            </section>

            <div class="border-t border-gray-100" />

            <!-- 路由权限 -->
            <section>
                <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-2 font-semibold text-gray-700">
                        <ArrowsRightLeftIcon class="w-5 h-5 text-purple-500" />
                        路由权限
                    </div>
                    <Space>
                        <Button size="mini" type="outline" @click="selectAllRoutes">全选</Button>
                        <Button size="mini" type="outline" status="danger" @click="clearAllRoutes">清空</Button>
                    </Space>
                </div>
                <div class="grid grid-cols-[1fr_40px_1fr] gap-3 items-stretch">
                    <!-- 可用路由 -->
                    <div
                        class="border border-dashed border-gray-300 rounded-xl p-3 bg-gray-50 flex flex-col min-h-[180px]">
                        <div class="text-xs font-medium text-gray-400 mb-2 uppercase tracking-wide">
                            可用路由 ({{ availableRoutes.length }})
                        </div>
                        <draggable v-model="availableRoutes" :group="{ name: 'route', pull: 'clone', put: false }"
                            :sort="false" item-key="(item: string) => item" class="flex-1 overflow-y-auto space-y-1">
                            <template #item="{ element }">
                                <div class="flex items-center justify-between px-3 py-1.5 bg-white border border-gray-100 rounded-lg text-sm cursor-grab hover:border-purple-400 hover:text-purple-600 hover:shadow-sm transition-all group"
                                    @click="addRoute(element)">
                                    <span class="font-mono text-xs">{{ element }}</span>
                                    <ChevronRightIcon
                                        class="w-4 h-4 text-gray-300 group-hover:text-purple-500 flex-shrink-0" />
                                </div>
                            </template>
                        </draggable>
                    </div>
                    <!-- 箭头指示 -->
                    <div class="hidden md:flex flex-col items-center justify-center text-gray-300 py-4">
                        <ArrowsRightLeftIcon class="w-5 h-5" />
                    </div>
                    <!-- 已选路由 -->
                    <div class="border border-purple-200 rounded-xl p-3 bg-purple-50/40 flex flex-col min-h-[180px]">
                        <div class="text-xs font-medium text-purple-500 mb-2 uppercase tracking-wide">
                            已选路由 ({{ userRoutes.length }})
                        </div>
                        <draggable v-model="userRoutes" group="route" item-key="(item: string) => item"
                            class="flex-1 overflow-y-auto space-y-1" @add="onRouteAdd">
                            <template #item="{ element }">
                                <div class="flex items-center justify-between px-3 py-1.5 bg-white border border-purple-100 rounded-lg text-sm cursor-grab hover:border-red-400 hover:text-red-500 hover:shadow-sm transition-all group"
                                    @click="removeRoute(element)">
                                    <span class="font-mono text-xs text-purple-700">{{ element }}</span>
                                    <ChevronLeftIcon
                                        class="w-4 h-4 text-gray-300 group-hover:text-red-400 flex-shrink-0" />
                                </div>
                            </template>
                        </draggable>
                    </div>
                </div>
            </section>
        </div>
    </Modal>
</template>
