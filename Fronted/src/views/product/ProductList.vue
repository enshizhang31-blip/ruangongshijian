<script setup lang="ts">
import { onMounted, ref, reactive, h } from 'vue'
import { productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { formatDate, formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Form, FormItem, Select, Message, Empty, Collapse, Skeleton } from '@arco-design/web-vue'
import type { Product, ProductCategory, Sku } from '@/types'
import { PlusIcon, PencilIcon, ChevronDownIcon } from '@heroicons/vue/24/outline'

const { loading, error, list, total, query, load, setPage, setKeyword } = usePageQuery(productApi.list)
const keyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()

// 分类相关
const categories = ref<ProductCategory[]>([])
const loadingCategories = ref(false)
const searchCategoryId = ref<number>()
const expandedSkus = ref<Record<number, boolean>>({})
const skuLoading = ref<Record<number, boolean>>({})
const skuData = ref<Record<number, Sku[]>>({})

const form = reactive<Partial<Product>>({
    name: '',
    categoryId: undefined,
    brand: '',
    imageUrl: '',
    images: '',
    description: '',
    status: 1,
})

const columns = [
    { title: '商品名称', dataIndex: 'name', width: 200 },
    { title: '分类', dataIndex: 'categoryName', width: 100 },
    { title: '品牌', dataIndex: 'brand', width: 100 },
    {
        title: '状态', dataIndex: 'status', width: 80, render: (status: number) =>
            h(Tag, { color: status === 1 ? 'green' : 'gray' }, () => status === 1 ? '启用' : '禁用')
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 160, render: (t: string) => t ? formatDate(t) : '-' },
    { title: '操作', slotName: 'actions', align: 'right', width: 120 },
]

// 获取分类名称
function getCategoryName(categoryId?: number): string {
    if (!categoryId) return '-'
    const cat = categories.value.find(c => c.id === categoryId)
    return cat?.name || '-'
}

// 展开/收起SKU
async function toggleSkus(productId: number) {
    if (expandedSkus.value[productId]) {
        expandedSkus.value[productId] = false
        return
    }
    if (skuData.value[productId]) {
        expandedSkus.value[productId] = true
        return
    }
    skuLoading.value[productId] = true
    expandedSkus.value[productId] = true
    try {
        const skus = await productApi.getSkus(productId)
        skuData.value[productId] = skus
    } catch {
        Message.error('获取SKU失败')
    } finally {
        skuLoading.value[productId] = false
    }
}

onMounted(async () => {
    load()
    await fetchCategories()
})

async function fetchCategories() {
    loadingCategories.value = true
    try {
        categories.value = await productApi.categories()
    } catch {
        Message.error('获取分类失败')
    } finally {
        loadingCategories.value = false
    }
}

function handleSearch() {
    Object.assign(query.value, {
        keyword: keyword.value,
        categoryId: searchCategoryId.value,
        page: 1,
    })
    load()
}

function handleReset() {
    keyword.value = ''
    searchCategoryId.value = undefined
    setKeyword('')
}

function handleAdd() {
    isEdit.value = false
    editingId.value = undefined
    Object.assign(form, { name: '', categoryId: undefined, brand: '', imageUrl: '', images: '', description: '', status: 1 })
    showModal.value = true
}

function handleEdit(record: Product) {
    isEdit.value = true
    editingId.value = record.id
    Object.assign(form, { ...record })
    showModal.value = true
}

async function handleSubmit() {
    if (!form.name || form.name.trim() === '') {
        Message.warning('请填写商品名称')
        return false
    }
    if (form.name.length > 128) {
        Message.warning('商品名称不能超过128个字符')
        return false
    }
    if (form.brand && form.brand.length > 64) {
        Message.warning('品牌名称不能超过64个字符')
        return false
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
    } catch (e: any) {
        Message.error(e?.message || '操作失败')
        return false
    }
}

async function handleDelete(id: number) {
    try {
        await productApi.delete(id)
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
                <Input v-model="keyword" placeholder="搜索商品名称..." class="w-64!" @press-enter="handleSearch">
                    <template #prefix><span class="text-gray-400">🔍</span></template>
                </Input>
                <Select v-model="searchCategoryId" placeholder="选择分类" class="w-48!" :loading="loadingCategories" allow-clear>
                    <Select.Option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
                </Select>
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

            <Table v-else :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 900 }">
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
    <Modal v-model:visible="showModal" :title="isEdit ? '编辑商品' : '新增商品'" :on-before-ok="handleSubmit" :width="500">
        <Form :model="form" layout="vertical">
            <FormItem label="商品名称" required>
                <Input v-model="form.name" placeholder="请输入商品名称" />
            </FormItem>
            <FormItem label="商品分类">
                <Select v-model="form.categoryId" placeholder="请选择分类" class="w-full" allow-clear>
                    <Select.Option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</Select.Option>
                </Select>
            </FormItem>
            <FormItem label="品牌">
                <Input v-model="form.brand" placeholder="请输入品牌" />
            </FormItem>
            <FormItem label="商品图片">
                <Input v-model="form.imageUrl" placeholder="请输入图片URL" />
            </FormItem>
            <FormItem label="商品描述">
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
