<script setup lang="ts">
import { onMounted, ref, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api'
import { formatMoney } from '@/utils/format'
import { Table, Button, Input, Space, Tag, Popconfirm, Card, Modal, Message, Empty, Select, Form, FormItem } from '@arco-design/web-vue'
import type { Product, Sku, Spec, SpecValue } from '@/types'
import { PlusIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const spuId = Number(route.params.id)

const loading = ref(false)
const spu = ref<Product | null>(null)

const skuList = ref<Sku[]>([])
const skuLoading = ref(false)

// 所有规格数据（缓存用于ID->名称映射）
const allSpecs = ref<Spec[]>([])
const specValueCache = ref<Record<number, SpecValue[]>>({})
const valueMap = ref<Record<number, SpecValue>>({})
const specMap = ref<Record<number, Spec>>({})

function buildSpecMaps(specs: Spec[]) {
    const vm: Record<number, SpecValue> = {}
    const sm: Record<number, Spec> = {}
    for (const s of specs) {
        sm[s.id] = s
        for (const v of s.values || []) vm[v.id] = v
    }
    valueMap.value = vm
    specMap.value = sm
}

async function getSpecValues(specId: number): Promise<SpecValue[]> {
    if (specValueCache.value[specId]) return specValueCache.value[specId]
    try {
        const vals = await productApi.getSpecValues(specId)
        specValueCache.value[specId] = vals
        for (const v of vals) valueMap.value[v.id] = v
        return vals
    } catch { return [] }
}

// 解析 specJson 为可读标签（兼容新旧格式）
function renderSpecTags(specJson?: string): { name: string; value: string }[] {
    if (!specJson) return []
    try {
        const obj = JSON.parse(specJson)
        return Object.entries(obj).map(([k, v]) => {
            const specId = Number(k); const valId = Number(v)
            if (!isNaN(specId) && !isNaN(valId) && valueMap.value[valId]) {
                return { name: specMap.value[specId]?.name || k, value: valueMap.value[valId].value }
            }
            return { name: k, value: String(v) }
        })
    } catch { return [] }
}

// 选中规格名时异步加载值
function onSpecSelected(row: SpecRow) {
    if (row.specId) getSpecValues(row.specId)
    row.valueId = undefined
}

// ====== 新增/编辑 SKU 表单 ======
const showFormModal = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()

interface SpecRow { specId: number | undefined; valueId: number | undefined }
const formModel = reactive({
    skuCode: '',
    specs: [{ specId: undefined, valueId: undefined }] as SpecRow[],
    price: 0,
    costPrice: undefined as number | undefined,
    unit: '件',
    imageUrl: '',
    status: 1,
})

    function handleAdd() {
    isEdit.value = false; editingId.value = undefined
    formModel.skuCode = ''
    formModel.specs = [{ specId: undefined, valueId: undefined }]
    formModel.price = 0
    formModel.costPrice = undefined
    formModel.unit = '件'
    formModel.imageUrl = ''
    formModel.status = 1
    showFormModal.value = true
}

function handleEdit(record: Sku) {
    isEdit.value = true; editingId.value = record.id
    formModel.skuCode = record.skuCode
    formModel.specs = []
    formModel.price = record.price
    formModel.costPrice = record.costPrice
    formModel.unit = record.unit || '件'
    formModel.imageUrl = record.imageUrl || ''
    formModel.status = record.status
    try {
        const obj = JSON.parse(record.specJson || '{}')
        for (const [k, v] of Object.entries(obj)) {
            const specId = Number(k); const valId = Number(v)
            if (specId && valId) formModel.specs.push({ specId, valueId: valId })
        }
    } catch { /* ignore */ }
    if (formModel.specs.length === 0) formModel.specs.push({ specId: undefined, valueId: undefined })
    showFormModal.value = true
}

const availableSpecs = computed(() => {
    const used = new Set(formModel.specs.map(r => r.specId).filter((id): id is number => !!id))
    return allSpecs.value.filter(s => !used.has(s.id))
})

function addSpecRow() {
    formModel.specs.push({ specId: undefined, valueId: undefined })
}

function removeSpecRow(index: number) {
    formModel.specs.splice(index, 1)
}

watch(() => formModel.specs.map(r => `${r.specId}-${r.valueId}`).join(','), () => {
    const obj: Record<string, string> = {}
    for (const row of formModel.specs) {
        if (row.specId && row.valueId) obj[String(row.specId)] = String(row.valueId)
    }
    if (!isEdit.value && !formModel.skuCode) {
        formModel.skuCode = autoGenerateSkuCode(obj)
    }
})

function autoGenerateSkuCode(_specIdObj: Record<string, string>): string {
    const abbr = spu.value?.name
        ? spu.value.name.replace(/[\u4e00-\u9fa5]/g, '').replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(0, 6) || 'SPU'
        : 'SPU'
    const id = editingId.value || Date.now() % 10000
    return `SKU-${abbr}-${id}-SPU${spuId}`
}

async function handleSubmit() {
    if (!spuId) { Message.warning('SPU不存在'); return false }
    if (!formModel.price || formModel.price <= 0) { Message.warning('请填写有效的价格'); return false }
    try {
        const obj: Record<string, string> = {}
        for (const row of formModel.specs) {
            if (row.specId && row.valueId) obj[String(row.specId)] = String(row.valueId)
        }
        const data: Partial<Sku> = {
            spuId,
            skuCode: formModel.skuCode,
            specJson: JSON.stringify(obj),
            price: formModel.price,
            costPrice: formModel.costPrice,
            unit: formModel.unit,
            imageUrl: formModel.imageUrl,
            status: formModel.status,
        }
        if (isEdit.value && editingId.value) {
            await productApi.updateSku({ ...data, id: editingId.value } as Sku)
            Message.success('更新成功')
        } else {
            await productApi.createSku(data as Sku)
            Message.success('创建成功')
        }
        loadSkus()
        return true
    } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

async function handleDelete(id: number) {
    try { await productApi.deleteSku(id); Message.success('删除成功'); loadSkus() }
    catch (e: any) { Message.error(e?.message || '删除失败') }
}

async function handleToggleSkuStatus(record: Sku) {
    const newStatus = record.status === 1 ? 0 : 1
    try { await productApi.updateSku({ ...record, status: newStatus } as Sku); Message.success('状态更新成功'); loadSkus() }
    catch (e: any) { Message.error(e?.message || '操作失败') }
}

async function loadSkus() {
    skuLoading.value = true
    try { skuList.value = await productApi.getSkus(spuId) }
    catch { Message.error('加载SKU失败') }
    finally { skuLoading.value = false }
}

// ====== 批量生成 SKU ======
const showBatchSkuModal = ref(false)
const batchSkuPrefix = ref('')
const batchSkuPrice = ref<number>(0)
const batchSkuCostPrice = ref<number>(0)
const selectedBatchSpecIds = ref<number[]>([])

async function handleOpenBatchSku() {
    batchSkuPrefix.value = ''
    batchSkuPrice.value = 0
    batchSkuCostPrice.value = 0
    selectedBatchSpecIds.value = []
    try { allSpecs.value = await productApi.getSpecs(); buildSpecMaps(allSpecs.value) } catch { allSpecs.value = [] }
    showBatchSkuModal.value = true
}

async function handleBatchGenerateSku() {
    if (selectedBatchSpecIds.value.length === 0) { Message.warning('请至少选择一个规格'); return false }
    try {
        await productApi.batchGenerateSkus({
            spuId, specIds: selectedBatchSpecIds.value,
            codePrefix: batchSkuPrefix.value || undefined,
            defaultPrice: batchSkuPrice.value || undefined,
            defaultCostPrice: batchSkuCostPrice.value || undefined,
        } as any)
        Message.success('批量生成SKU成功')
        showBatchSkuModal.value = false
        loadSkus()
        return true
    } catch (e: any) { Message.error(e?.message || '操作失败'); return false }
}

onMounted(async () => {
    loading.value = true
    try { spu.value = await productApi.getById(spuId) }
    catch { Message.error('加载SPU失败') }
    finally { loading.value = false }
    loadSkus()
    try { const specs = await productApi.getSpecs(); allSpecs.value = specs; buildSpecMaps(specs) }
    catch { /* ignore */ }
})
</script>

<template>
    <div class="p-4 lg:p-6">
        <div class="mb-6">
            <Button type="text" size="small" class="mb-2" @click="router.push('/product')">← 返回SPU列表</Button>
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 class="text-xl lg:text-2xl font-bold text-gray-800">{{ spu?.name || '加载中...' }}</h1>
                    <div class="flex items-center gap-3 mt-1 text-sm text-gray-500">
                        <span v-if="spu?.categoryName">分类：{{ spu.categoryName }}</span>
                        <span v-if="spu?.brand">品牌：{{ spu.brand }}</span>
                        <Tag v-if="spu" :color="spu.status === 1 ? 'green' : 'gray'">{{ spu.status === 1 ? '上架' : '下架'
                            }}</Tag>
                    </div>
                </div>
            </div>
        </div>

        <Card>
            <div class="flex items-center justify-between mb-4">
                <h2 class="text-lg font-semibold text-gray-700">SKU 列表</h2>
                <Space>
                    <Button type="outline" @click="handleOpenBatchSku">
                        <template #icon>
                            <PlusIcon class="w-4 h-4" />
                        </template>批量生成SKU
                    </Button>
                    <Button type="primary" @click="handleAdd">
                        <template #icon>
                            <PlusIcon class="w-4 h-4" />
                        </template>新增SKU
                    </Button>
                </Space>
            </div>

            <div v-if="skuLoading && skuList.length === 0" class="text-center py-8"><span
                    class="text-gray-400">加载中...</span></div>
            <div v-else-if="!skuLoading && skuList.length === 0" class="text-center py-8">
                <Empty description="暂无SKU，请先创建或批量生成" />
            </div>

            <Table v-else :loading="skuLoading" :columns="[
                { title: '规格组合', slotName: 'specJson', width: 260 },
                { title: 'SKU编码', dataIndex: 'skuCode', width: 140 },
                { title: '销售价', dataIndex: 'price', width: 100 },
                { title: '成本价', dataIndex: 'costPrice', width: 100 },
                { title: '库存', dataIndex: 'stock', width: 80, align: 'center' as const },
                { title: '状态', dataIndex: 'status', width: 80 },
                { title: '操作', slotName: 'actions', align: 'right', width: 200, fixed: 'right' },
            ]" :data="skuList" :pagination="false" :scroll="{ x: 1000 }">
                <template #specJson="{ record }">
                    <Space>
                        <Tag v-for="tag in renderSpecTags(record.specJson)" :key="tag.name">{{ tag.name }}: {{ tag.value
                            }}</Tag>
                    </Space>
                </template>
                <template #price="{ record }">{{ formatMoney(record.price) }}</template>
                <template #costPrice="{ record }">{{ record.costPrice ? formatMoney(record.costPrice) : '-'
                    }}</template>
                <template #stock="{ record }"><span
                        :class="record.stock ? 'text-green-600 font-medium' : 'text-red-400'">{{ record.stock ?? 0
                        }}</span></template>
                <template #status="{ record }">
                    <Tag :color="record.status === 1 ? 'green' : 'gray'">{{ record.status === 1 ? '启用' : '禁用' }}</Tag>
                </template>
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleEdit(record)">编辑</Button>
                        <Button type="text" size="small" @click="handleToggleSkuStatus(record)">{{ record.status === 1 ?
                            '禁用' : '启用' }}</Button>
                        <Button type="text" size="small" class="text-purple-600"
                            @click="router.push(`/sn/sku/${record.id}?spuId=${spuId}`)">SN码</Button>
                        <Popconfirm title="确定删除该SKU？" @ok="handleDelete(record.id)"><Button type="text" status="danger"
                                size="small">删除</Button></Popconfirm>
                    </Space>
                </template>
            </Table>
        </Card>
    </div>

    <!-- 新增/编辑 SKU 弹窗 -->
    <Modal v-model:visible="showFormModal" :title="isEdit ? '编辑SKU' : '新增SKU'" :on-before-ok="handleSubmit"
        :width="580">
        <a-form :model="formModel">
            <a-form-item field="skuCode" label="SKU编码">
                <a-input :model-value="formModel.skuCode" placeholder="自动生成" disabled />
            </a-form-item>

            <a-form-item label="规格组合" :content-flex="false" :merge-props="false">
                <a-space direction="vertical" fill>
                    <a-form-item v-for="(spec, index) in formModel.specs" :key="index"
                        :field="`specs[${index}].valueId`" no-style>
                        <a-space>
                            <Select v-model="formModel.specs[index].specId" placeholder="选择规格名" style="width:120px"
                                @change="onSpecSelected(formModel.specs[index])">
                                <Select.Option v-for="s in availableSpecs" :key="s.id" :value="s.id">{{ s.name }}
                                </Select.Option>
                            </Select>
                            <Select v-model="formModel.specs[index].valueId" placeholder="选择规格值" style="width:200px"
                                :disabled="!formModel.specs[index].specId">
                                <Select.Option v-for="v in (specValueCache[formModel.specs[index].specId || 0] || [])"
                                    :key="v.id" :value="v.id">{{ v.value }}</Select.Option>
                            </Select>
                            <Button type="text" status="danger" size="small" @click="removeSpecRow(index)">删除</Button>
                        </a-space>
                    </a-form-item>
                    <Button type="text" size="small" @click="addSpecRow">+ 添加规格</Button>
                    <div v-if="allSpecs.length === 0" class="text-sm text-gray-400">
                        暂无规格数据，请先前往 <a class="text-blue-500 cursor-pointer"
                            @click="router.push('/product/spec')">规格管理</a>
                        创建规格
                    </div>
                </a-space>
            </a-form-item>

            <a-form-item field="price" label="销售价格">
                <a-input v-model="formModel.price" type="number" placeholder="0.00" />
            </a-form-item>
            <a-form-item field="costPrice" label="成本价">
                <a-input v-model="formModel.costPrice" type="number" placeholder="0.00" />
            </a-form-item>
            <a-form-item field="unit" label="单位">
                <a-input v-model="formModel.unit" placeholder="件" />
            </a-form-item>
            <a-form-item field="status" label="状态">
                <Select v-model="formModel.status" style="width:140px">
                    <Select.Option :value="1">启用</Select.Option>
                    <Select.Option :value="0">禁用</Select.Option>
                </Select>
            </a-form-item>
        </a-form>
    </Modal>

    <!-- 批量生成SKU 弹窗 -->
    <Modal v-model:visible="showBatchSkuModal" title="批量生成SKU" :on-before-ok="handleBatchGenerateSku" :width="500">
        <div class="flex flex-col gap-4">
            <div class="flex items-center gap-4">
                <div class="w-20 text-sm text-gray-500">SKU前缀</div>
                <Input v-model="batchSkuPrefix" placeholder="如 IP15" class="flex-1" />
            </div>
            <div class="flex items-center gap-4">
                <div class="w-20 text-sm text-gray-500">默认价格</div>
                <Input v-model="batchSkuPrice" type="number" placeholder="0.00" class="flex-1" />
            </div>
            <div class="flex items-center gap-4">
                <div class="w-20 text-sm text-gray-500">默认成本</div>
                <Input v-model="batchSkuCostPrice" type="number" placeholder="0.00" class="flex-1" />
            </div>
            <div class="flex items-center gap-4">
                <div class="w-20 text-sm text-gray-500">规格选择</div>
                <Select v-model="selectedBatchSpecIds" placeholder="选择规格（系统自动做笛卡尔积）" class="flex-1" multiple>
                    <Select.Option v-for="spec in allSpecs" :key="spec.id" :value="spec.id">{{ spec.name }}
                    </Select.Option>
                </Select>
            </div>
            <p class="text-xs text-gray-400">系统将根据所选规格的所有值做笛卡尔积，自动生成所有SKU组合。</p>
        </div>
    </Modal>
</template>