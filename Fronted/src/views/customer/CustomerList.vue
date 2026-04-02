<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { customerApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Form, FormItem, Select, InputNumber, Message } from '@arco-design/web-vue'
import type { Customer } from '@/types'
import { PlusIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(customerApi.list)
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()

const form = reactive<Partial<Customer>>({
    name: '',
    phone: '',
    email: '',
    address: '',
    customerType: 1,
    level: 1,
    balance: 0,
    remark: '',
    status: 1,
})

const columns = [
    { title: '客户名称', dataIndex: 'name' },
    { title: '联系方式', dataIndex: 'phone' },
    {
        title: '客户类型', dataIndex: 'customerType', render: (type: number) =>
            Tag.color(type === 1 ? 'arcoblue' : 'purple')(type === 1 ? '个人' : '企业')
    },
    { title: '等级', dataIndex: 'level' },
    { title: '余额', dataIndex: 'balance', render: (balance: number) => `¥${balance || 0}` },
    {
        title: '状态', dataIndex: 'status', render: (status: number) =>
            status === 1 ? Tag.color('green')('正常') : Tag.color('gray')('禁用')
    },
    { title: '操作', slotName: 'actions', align: 'right' },
]

onMounted(() => {
    load()
})

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
    Object.assign(form, { name: '', phone: '', email: '', address: '', customerType: 1, level: 1, balance: 0, remark: '', status: 1 })
    showModal.value = true
}

function handleEdit(record: Customer) {
    isEdit.value = true
    editingId.value = record.id
    Object.assign(form, { ...record })
    showModal.value = true
}

async function handleSubmit() {
    if (!form.name) {
        Message.warning('请填写客户名称')
        return
    }
    try {
        if (isEdit.value && editingId.value) {
            await customerApi.update({ ...form, id: editingId.value } as Customer)
            Message.success('更新成功')
        } else {
            await customerApi.create(form as Customer)
            Message.success('创建成功')
        }
        showModal.value = false
        load()
    } catch {
        Message.error('操作失败')
    }
}

async function handleDelete(id: number) {
    await customerApi.delete(id)
    Message.success('删除成功')
    load()
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">客户管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理客户资料</p>
            </div>
            <Button type="primary" @click="handleAdd">
                <template #icon>
                    <PlusIcon class="w-4 h-4" />
                </template>
                新增客户
            </Button>
        </div>

        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索客户名称..." class="!w-64" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Button type="primary" @click="handleSearch">搜索</Button>
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <Card>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 800 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleEdit(record)">
                            <PencilIcon class="w-4 h-4" />
                        </Button>
                        <Popconfirm title="确定删除该客户？" @ok="handleDelete(record.id)">
                            <Button type="text" status="danger" size="small">
                                删除
                            </Button>
                        </Popconfirm>
                    </Space>
                </template>
            </Table>

            <div class="flex justify-end mt-4">
                <Space direction="horizontal">
                    <span class="text-sm text-gray-500">共 {{ total }} 条</span>
                    <Button :disabled="query.page <= 1" @click="setPage(query.page - 1)">上一页</Button>
                    <span class="text-sm py-2">第 {{ query.page }} / {{ Math.ceil(total / query.pageSize) || 1 }}
                        页</span>
                    <Button :disabled="query.page >= Math.ceil(total / query.pageSize)"
                        @click="setPage(query.page + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal v-model:visible="showModal" :title="isEdit ? '编辑客户' : '新增客户'" @ok="handleSubmit" :width="500">
        <Form :model="form" layout="vertical">
            <FormItem label="客户名称" required>
                <Input v-model="form.name" placeholder="请输入客户名称" />
            </FormItem>
            <FormItem label="联系方式">
                <Input v-model="form.phone" placeholder="请输入手机号" />
            </FormItem>
            <FormItem label="邮箱">
                <Input v-model="form.email" placeholder="请输入邮箱" />
            </FormItem>
            <FormItem label="地址">
                <Input v-model="form.address" placeholder="请输入地址" />
            </FormItem>
            <FormItem label="客户类型">
                <Select v-model="form.customerType" class="w-full">
                    <Select.Option :value="1">个人</Select.Option>
                    <Select.Option :value="2">企业</Select.Option>
                </Select>
            </FormItem>
            <FormItem label="会员等级">
                <InputNumber v-model="form.level" :min="1" :max="10" class="w-full" />
            </FormItem>
            <FormItem label="余额">
                <InputNumber v-model="form.balance" :min="0" :precision="2" class="w-full" />
            </FormItem>
            <FormItem label="备注">
                <Input v-model="form.remark" placeholder="备注信息" :rows="2" />
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
