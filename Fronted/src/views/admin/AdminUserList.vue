<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { adminApi, departmentApi, roleApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Card, Modal, Form, FormItem, Select, Popconfirm, Message, Spin, Empty } from '@arco-design/web-vue'
import type { AdminUser, Department, Role } from '@/types'
import { PlusIcon, PencilIcon, KeyIcon } from '@heroicons/vue/24/outline'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(adminApi.list)
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const departments = ref<Department[]>([])
const roles = ref<Role[]>([])
const loadingDepts = ref(false)
const loadingRoles = ref(false)

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
    { title: '操作', slotName: 'actions', align: 'right', width: 150 },
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
        return
    }
    if (!isEdit.value && !form.password) {
        Message.warning('请填写密码')
        return
    }
    if (!form.realName) {
        Message.warning('请填写姓名')
        return
    }
    if (!isEdit.value && !form.roleId) {
        Message.warning('请选择角色')
        return
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
            await adminApi.update(updateData as any)
            Message.success('更新成功')
        } else {
            // 新增时提交完整数据
            await adminApi.create(form as any)
            Message.success('创建成功')
        }
        showModal.value = false
        load()
    } catch (e: any) {
        Message.error(e?.message || '操作失败')
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
                        <Button type="text" size="small" @click="handleEdit(record)">
                            <PencilIcon class="w-4 h-4" />
                        </Button>
                        <Popconfirm title="确定重置该员工密码？" @ok="handleResetPassword(record.id)">
                            <Button type="text" size="small">
                                <KeyIcon class="w-4 h-4" />
                            </Button>
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
                    <span class="text-sm py-2">第 {{ query.page || 1 }} / {{ Math.ceil(total / (query.pageSize || 20)) ||
                        1 }}
                        页</span>
                    <Button :disabled="(query.page || 1) >= Math.ceil(total / (query.pageSize || 20))"
                        @click="setPage((query.page || 1) + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <Modal v-model:visible="showModal" :title="isEdit ? '编辑员工' : '新增员工'" @ok="handleSubmit" :width="500">
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
            <FormItem label="角色" required>
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
</template>
