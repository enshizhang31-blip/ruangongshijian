<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { customerApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Form, FormItem, Select, Message, Empty } from '@arco-design/web-vue'
import type { Customer } from '@/types'
import { PlusIcon, PencilIcon, EyeIcon } from '@heroicons/vue/24/outline'
import { formatMoney, formatDate } from '@/utils/format'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(customerApi.list)
const keyword = ref('')
const showModal = ref(false)
const showDetailModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const viewingCustomer = ref<Customer | null>(null)

const form = reactive<Partial<Customer>>({
    nickname: '',
    phone: '',
    avatar: '',
    memberLevel: 1,
    status: 1,
})

const columns = [
    { title: '昵称', dataIndex: 'nickname' },
    { title: '手机号', dataIndex: 'phone' },
    {
        title: '会员等级', dataIndex: 'memberLevel', render: (level: number) => {
            const map: Record<number, string> = { 1: '普通', 2: '银卡', 3: '金卡', 4: '钻石' }
            return h(Tag, { color: level >= 3 ? 'gold' : 'arcoblue' }, () => map[level] || '普通')
        }
    },
    {
        title: '余额', dataIndex: 'balance', render: (balance: number | string) => `¥${formatMoney(balance)}`
    },
    {
        title: '状态', dataIndex: 'status', render: (status: number) =>
            h(Tag, { color: status === 1 ? 'green' : 'gray' }, () => status === 1 ? '正常' : '禁用')
    },
    { title: '操作', slotName: 'actions', align: 'right', fixed: 'right' },
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
    Object.assign(form, { nickname: '', phone: '', avatar: '', memberLevel: 1, status: 1 })
    showModal.value = true
}

function handleEdit(record: Customer) {
    isEdit.value = true
    editingId.value = record.id
    Object.assign(form, { ...record })
    showModal.value = true
}

function handleView(record: Customer) {
    viewingCustomer.value = record
    showDetailModal.value = true
}

async function handleSubmit() {
    if (!form.phone) {
        Message.warning('请填写手机号')
        return false
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
    } catch (e: any) {
        Message.error(e?.message || '操作失败')
        return false
    }
}

async function handleDelete(id: number) {
    try {
        await customerApi.delete(id)
        Message.success('删除成功')
        load()
    } catch (e: any) {
        Message.error(e?.message || '删除失败')
    }
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
                <Input v-model="keyword" placeholder="搜索客户名称..." class="w-64!" @press-enter="handleSearch">
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

            <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 800 }">
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleView(record)">
                            <EyeIcon class="w-4 h-4" />
                        </Button>
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

    <!-- 新增/编辑弹窗 -->
    <Modal v-model:visible="showModal" :title="isEdit ? '编辑客户' : '新增客户'" :on-before-ok="handleSubmit" :width="500">
        <Form :model="form" layout="vertical">
            <FormItem label="昵称">
                <Input v-model="form.nickname" placeholder="请输入昵称" />
            </FormItem>
            <FormItem label="手机号" required>
                <Input v-model="form.phone" placeholder="请输入手机号" />
            </FormItem>
            <FormItem label="头像URL">
                <Input v-model="form.avatar" placeholder="请输入头像URL" />
            </FormItem>
            <FormItem label="会员等级">
                <Select v-model="form.memberLevel" class="w-full">
                    <Select.Option :value="1">普通会员</Select.Option>
                    <Select.Option :value="2">银卡会员</Select.Option>
                    <Select.Option :value="3">金卡会员</Select.Option>
                    <Select.Option :value="4">钻石会员</Select.Option>
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

    <!-- 客户详情弹窗 -->
    <Modal v-model:visible="showDetailModal" title="客户详情" :width="550" :footer="false">
        <div v-if="viewingCustomer">
            <!-- 基本信息 -->
            <div class="mb-4">
                <div class="flex items-center gap-4 mb-4">
                    <img v-if="viewingCustomer.avatar" :src="viewingCustomer.avatar"
                        class="w-16 h-16 rounded-full object-cover" />
                    <div v-else
                        class="w-16 h-16 rounded-full bg-gray-200 flex items-center justify-center text-gray-400">
                        {{ viewingCustomer.nickname?.charAt(0) || '?' }}
                    </div>
                    <div>
                        <div class="text-lg font-medium">{{ viewingCustomer.nickname || '-' }}</div>
                        <Tag :color="viewingCustomer.status === 1 ? 'green' : 'gray'" size="small">
                            {{ viewingCustomer.status === 1 ? '正常' : '禁用' }}
                        </Tag>
                    </div>
                </div>
            </div>

            <!-- 会员信息 -->
            <div class="bg-gray-50 rounded-lg p-4 mb-4">
                <h4 class="text-sm font-medium text-gray-700 mb-3">会员信息</h4>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <div class="text-xs text-gray-500">会员等级</div>
                        <div class="text-sm">
                            <Tag
                                :color="viewingCustomer.memberLevel === 4 ? 'gold' : viewingCustomer.memberLevel === 3 ? 'orange' : 'arcoblue'">
                                {{ ['', '普通会员', '银卡会员', '金卡会员', '钻石会员'][viewingCustomer.memberLevel || 1] }}
                            </Tag>
                        </div>
                    </div>
                    <div>
                        <div class="text-xs text-gray-500">手机号</div>
                        <div class="text-sm">{{ viewingCustomer.phone || '-' }}</div>
                    </div>
                    <div>
                        <div class="text-xs text-gray-500">账户余额</div>
                        <div class="text-sm font-medium text-green-600">¥{{ formatMoney(viewingCustomer.balance || 0) }}
                        </div>
                    </div>
                    <div>
                        <div class="text-xs text-gray-500">积分余额</div>
                        <div class="text-sm">{{ viewingCustomer.points || 0 }}</div>
                    </div>
                </div>
            </div>

            <!-- 消费统计 -->
            <div class="bg-gray-50 rounded-lg p-4 mb-4">
                <h4 class="text-sm font-medium text-gray-700 mb-3">消费统计</h4>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <div class="text-xs text-gray-500">累计消费</div>
                        <div class="text-sm font-medium">¥{{ formatMoney(viewingCustomer.totalConsume || 0) }}</div>
                    </div>
                    <div>
                        <div class="text-xs text-gray-500">累计积分</div>
                        <div class="text-sm">{{ viewingCustomer.totalPoints || 0 }}</div>
                    </div>
                </div>
            </div>

            <!-- 时间信息 -->
            <div class="text-xs text-gray-400">
                注册时间: {{ viewingCustomer.createdAt ? formatDate(viewingCustomer.createdAt) : '-' }}
            </div>
        </div>
    </Modal>
</template>
