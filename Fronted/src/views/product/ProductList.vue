<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatMoney } from '@/utils/format'
import { Table, Button, Input, InputNumber, Space, Tag, Popconfirm, Card, Modal, Form, FormItem, Select, Message } from '@arco-design/web-vue'
import type { Product } from '@/types'
import { PlusIcon, PencilIcon } from '@heroicons/vue/24/outline'

const { loading, list, total, query, load, setPage, setKeyword } = usePageQuery(productApi.list)
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()

const form = reactive<Partial<Product>>({
    name: '',
    categoryId: undefined,
    brand: '',
    price: 0,
    stock: 0,
    unit: '',
    images: '',
    description: '',
    status: 1,
})

const columns = [
    { title: '商品名称', dataIndex: 'name' },
    { title: '品牌', dataIndex: 'brand' },
    { title: '分类', dataIndex: 'categoryName' },
    { title: '价格', dataIndex: 'price', render: (value: number) => `¥${formatMoney(value)}` },
    { title: '库存', dataIndex: 'stock' },
    { title: '单位', dataIndex: 'unit' },
    {
        title: '状态', dataIndex: 'status', render: (status: number) =>
            h(Tag, { color: status === 1 ? 'green' : 'gray' }, () => status === 1 ? '启用' : '禁用')
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
    Object.assign(form, { name: '', categoryId: undefined, brand: '', price: 0, stock: 0, unit: '', images: '', description: '', status: 1 })
    showModal.value = true
}

function handleEdit(record: Product) {
    isEdit.value = true
    editingId.value = record.id
    Object.assign(form, { ...record })
    showModal.value = true
}

async function handleSubmit() {
    if (!form.name) {
        Message.warning('请填写商品名称')
        return
    }
    try {
        if (isEdit.value && editingId.value) {
            await productApi.update({ ...form, id: editingId.value } as Product)
            Message.success('更新成功')
        } else {
            await productApi.create(form as Product)
            Message.success('创建成功')
        }
        showModal.value = false
        load()
    } catch {
        Message.error('操作失败')
    }
}

async function handleDelete(id: number) {
    await productApi.delete(id)
    Message.success('删除成功')
    load()
}
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">商品管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理商品信息</p>
            </div>
            <Button type="primary" @click="handleAdd">
                <template #icon>
                    <PlusIcon class="w-4 h-4" />
                </template>
                新增商品
            </Button>
        </div>

        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Input v-model="keyword" placeholder="搜索商品名称..." class="!w-64" @press-enter="handleSearch">
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
                        <Popconfirm title="确定删除该商品？" @ok="handleDelete(record.id)">
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
                    <span class="text-sm py-2">第 {{ query.page }} / {{ Math.ceil(total / (query.pageSize || 20)) || 1 }}
                        页</span>
                    <Button :disabled="query.page >= Math.ceil(total / (query.pageSize || 20))"
                        @click="setPage(query.page + 1)">下一页</Button>
                </Space>
            </div>
        </Card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Modal v-model:visible="showModal" :title="isEdit ? '编辑商品' : '新增商品'" @ok="handleSubmit" :width="500">
        <Form :model="form" layout="vertical">
            <FormItem label="商品名称" required>
                <Input v-model="form.name" placeholder="请输入商品名称" />
            </FormItem>
            <FormItem label="商品分类">
                <Select v-model="form.categoryId" placeholder="请选择分类" class="w-full">
                    <!-- 分类选项后续动态加载 -->
                </Select>
            </FormItem>
            <FormItem label="价格">
                <InputNumber v-model="form.price" :min="0" :precision="2" class="w-full" />
            </FormItem>
            <FormItem label="库存">
                <InputNumber v-model="form.stock" :min="0" class="w-full" />
            </FormItem>
            <FormItem label="单位">
                <Input v-model="form.unit" placeholder="如：个、箱、件" />
            </FormItem>
            <FormItem label="描述">
                <Input v-model="form.description" placeholder="商品描述" :rows="3" />
            </FormItem>
            <FormItem label="状态">
                <Select v-model="form.status" class="w-full">
                    <Select.Option :value="1">启用</Select.Option>
                    <Select.Option :value="0">禁用</Select.Option>
                </Select>
            </FormItem>
        </Form>
    </Modal>
</template>
