<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { snApi, productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Space, Tag, Card, Select, Message, Pagination, Modal, DatePicker } from '@arco-design/web-vue'
import { PlusIcon, BarsArrowUpIcon, QrCodeIcon } from '@heroicons/vue/24/outline'
import { useRouter } from 'vue-router'
import SnAddModal from '@/components/SnAddModal.vue'
import SnEditModal from '@/components/SnEditModal.vue'
import type { SnCode, Sku } from '@/types'

const { loading, list, total, query, load, setPage, setPageSize } = usePageQuery(snApi.list)
const router = useRouter()
const showAddModal = ref(false)
const showEditModal = ref(false)
const editTarget = ref<{ id: number; status: number }>({ id: 0, status: 0 })

// ============ 多选状态 ============
const selectedRowKeys = ref<(string | number)[]>([])
const selectedRows = ref<SnCode[]>([])
// arco Table 的 v-model:selected-keys 会自动维护 selectedRowKeys，
// 这里只需要在 onChange 时同步 selectedRows
function onSelectChange(keys: (string | number)[], records: SnCode[]) {
    selectedRows.value = records
}
// 手动「全选当前页」：因为 arco 全选框在某些版本/配置下不触发，
// 这里提供按钮式兜底
function selectAllOnPage() {
    if (!list.value || list.value.length === 0) return
    const pageKeys = list.value.map((it: any) => it.id)
    const merged = Array.from(new Set([...selectedRowKeys.value, ...pageKeys]))
    selectedRowKeys.value = merged
    const pageRows = list.value.filter((it: any) => pageKeys.includes(it.id))
    const existingIds = new Set(selectedRows.value.map((r: any) => r.id))
    const newRows = [
        ...selectedRows.value.filter((r: any) => merged.includes(r.id)),
        ...pageRows.filter((it: any) => !existingIds.has(it.id)),
    ]
    selectedRows.value = newRows
    Message.success(`已选中当前页 ${pageKeys.length} 条，累计 ${merged.length} 条`)
}
function clearSelection() {
    selectedRowKeys.value = []
    selectedRows.value = []
}

const skuList = ref<Sku[]>([])
const loadingSkus = ref(false)
const selectedSkuCode = ref<string>('')

const searchForm = reactive({
    status: undefined as number | undefined,
    dateRange: [] as string[],
})

// ============ 9 状态映射（与后端一致） ============
const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '在库', color: 'green' },
    1: { label: '锁定', color: 'orange' },
    2: { label: '已售', color: 'arcoblue' },
    3: { label: '已发货', color: 'blue' },
    4: { label: '已签收', color: 'green' },
    5: { label: '已完成', color: 'gray' },
    6: { label: '已作废', color: 'red' },
    7: { label: '退货中', color: 'orange' },
    8: { label: '已退货', color: 'purple' },
}

const statusOptions = [
    { label: '全部状态', value: undefined },
    { label: '在库', value: 0 },
    { label: '锁定', value: 1 },
    { label: '已售', value: 2 },
    { label: '已发货', value: 3 },
    { label: '已签收', value: 4 },
    { label: '已完成', value: 5 },
    { label: '已作废', value: 6 },
    { label: '退货中', value: 7 },
    { label: '已退货', value: 8 },
]

onMounted(() => { load(); loadSkus() })

async function loadSkus() {
    loadingSkus.value = true
    try {
        const goodsRes: any = await productApi.list({ pageSize: 100 })
        const all: Sku[] = []
        for (const g of goodsRes.list || []) {
            try { all.push(...await productApi.getSkus(g.id)) } catch { /* ignore */ }
        }
        skuList.value = all
    } catch { /* silent */ }
    finally { loadingSkus.value = false }
}

function handleAdvancedSearch() {
    query.value = {
        page: 1,
        pageSize: query.value.pageSize || 20,
        keyword: selectedSkuCode.value || undefined,
        status: searchForm.status,
        startDate: searchForm.dateRange?.[0] || undefined,
        endDate: searchForm.dateRange?.[1] || undefined,
    }
    clearSelection()
    load()
}
function handleReset() {
    selectedSkuCode.value = ''
    searchForm.status = undefined
    searchForm.dateRange = []
    query.value = { page: 1, pageSize: 20 }
    clearSelection()
    load()
}
function handlePageChange(p: number) { setPage(p) }
function handlePageSizeChange(size: number) { setPageSize(size); clearSelection() }

function handleOpenAdd() { showAddModal.value = true }

function handleOpenEdit(record: any) {
    editTarget.value = { id: record.id, status: record.status }
    showEditModal.value = true
}

// ============ 单个二维码预览（Canvas） ============
const showBarcodeModal = ref(false)
const barcodeSn = ref('')
const barcodeCanvasRef = ref<HTMLCanvasElement | null>(null)

function openBarcodeModal(snCode: string) {
    barcodeSn.value = snCode
    showBarcodeModal.value = true
    setTimeout(() => renderQrcode(snCode), 100)
}

async function renderQrcode(snCode: string) {
    try {
        if (barcodeCanvasRef.value) {
            const QRCode = (await import('qrcode')).default
            await QRCode.toCanvas(barcodeCanvasRef.value, snCode, {
                width: 240,
                margin: 2,
                errorCorrectionLevel: 'M',
                color: { dark: '#000000', light: '#ffffff' }
            })
        }
    } catch (e) {
        console.error('二维码渲染失败', e)
        Message.error('二维码渲染失败')
    }
}

function downloadBarcodePng() {
    if (!barcodeCanvasRef.value) return
    try {
        const canvas = barcodeCanvasRef.value
        const pngUrl = canvas.toDataURL('image/png')
        const link = document.createElement('a')
        link.href = pngUrl
        link.download = `SN-${barcodeSn.value}.png`
        link.click()
        Message.success('二维码已下载')
    } catch (e) {
        console.error('下载失败', e)
        Message.error('下载失败')
    }
}

// ============ 批量打印二维码（在新窗口打开打印页） ============
// 修复：不再用 document.write 注入跨域 <script>，改为父窗口用本地 qrcode 包
// 预渲染成 dataURL 后再写入打印页。打印页只渲染 <img>，完全离网可用。
async function batchExportBarcode() {
    if (!list.value || list.value.length === 0) {
        Message.warning('当前列表为空，请先筛选数据')
        return
    }

    const itemsRaw = list.value.map((item: any) => ({
        sn: item.snCode || '',
        name: item.spuName || '',
        status: item.status ?? 0,
    }))

    Message.loading('正在生成二维码...')
    let itemsWithQr: any[]
    try {
        const QRCode = (await import('qrcode')).default
        itemsWithQr = await Promise.all(itemsRaw.map(async it => {
            if (!it.sn) return { ...it, qr: '' }
            try {
                const dataUrl = await QRCode.toDataURL(it.sn, {
                    width: 160,
                    margin: 1,
                    errorCorrectionLevel: 'M',
                    color: { dark: '#000000', light: '#ffffff' }
                })
                return { ...it, qr: dataUrl }
            } catch (_) {
                return { ...it, qr: '' }
            }
        }))
    } finally {
        Message.clear()
    }

    const printWindow = window.open('', '_blank')
    if (!printWindow) {
        Message.error('请允许弹窗以打开打印页面')
        return
    }

    const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>SN码二维码打印</title>
      <style>
        body { margin: 0; padding: 20px; font-family: Arial, sans-serif; background: #fafafa; }
        .qrcode-item {
          display: inline-block; text-align: center; margin: 12px; padding: 12px;
          border: 1px solid #ddd; border-radius: 4px; background: #fff; vertical-align: top;
          width: 200px; box-sizing: border-box;
        }
        .qrcode-item .name {
          font-size: 12px; color: #333; margin-bottom: 8px; max-width: 180px;
          overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 600;
        }
        .qrcode-item img {
          display: block; margin: 0 auto; width: 160px; height: 160px;
        }
        .qrcode-item .placeholder {
          width: 160px; height: 160px; line-height: 160px;
          background: #f5f5f5; color: #999; font-size: 12px;
          border: 1px dashed #ccc; margin: 0 auto;
        }
        .qrcode-item .sn {
          font-size: 11px; color: #666; margin-top: 6px; font-family: 'Courier New', monospace;
          word-break: break-all; max-width: 180px;
        }
        .qrcode-item .status {
          display: inline-block; font-size: 10px; padding: 2px 8px; border-radius: 10px;
          background: #52c41a; color: #fff; margin-top: 4px;
        }
        @media print {
          .no-print { display: none; }
          body { padding: 0; background: #fff; }
          .qrcode-item { page-break-inside: avoid; }
        }
      </style>
    </head>
    <body>
      <div class="no-print" style="margin-bottom:20px; padding: 12px; background: #fff; border-radius: 4px;">
        <button onclick="window.print()" style="padding:8px 20px;font-size:14px;background:#165dff;color:#fff;border:none;border-radius:4px;cursor:pointer;">🖨️ 打印二维码</button>
        <span style="margin-left:12px;color:#666;">共 ${itemsWithQr.length} 个 SN 码 · 标签尺寸建议: 50mm × 30mm</span>
      </div>
      <div id="container"></div>
      <script>
        const items = ${JSON.stringify(itemsWithQr)};
        const container = document.getElementById('container');
        const statusNameMap = { 0: '在库', 1: '锁定', 2: '已售', 3: '已发货', 4: '已签收', 5: '已完成', 6: '已作废', 7: '退货中', 8: '已退货' };
        items.forEach((item) => {
          const div = document.createElement('div');
          div.className = 'qrcode-item';

          const nameDiv = document.createElement('div');
          nameDiv.className = 'name';
          nameDiv.textContent = item.name || '未命名商品';
          div.appendChild(nameDiv);

          if (item.qr) {
            const img = document.createElement('img');
            img.src = item.qr;
            img.alt = item.sn || '';
            div.appendChild(img);
          } else {
            const ph = document.createElement('div');
            ph.className = 'placeholder';
            ph.textContent = item.sn ? '生成失败' : 'SN 为空';
            div.appendChild(ph);
          }

          const snDiv = document.createElement('div');
          snDiv.className = 'sn';
          snDiv.textContent = item.sn;
          div.appendChild(snDiv);

          const statusDiv = document.createElement('div');
          statusDiv.className = 'status';
          statusDiv.textContent = statusNameMap[item.status] || '在库';
          div.appendChild(statusDiv);

          container.appendChild(div);
        });
      <\/script>
    </body>
    </html>
    `
    printWindow.document.write(html)
    printWindow.document.close()
    Message.success(`已生成 ${itemsWithQr.length} 个二维码, 标签可贴在商品上用摄像头扫描`)
}

const columns = [
    { title: 'ID', dataIndex: 'id', width: 80, fixed: 'left' as const },
    { title: 'SN码', dataIndex: 'snCode', width: 180 },
    { title: '商品名称', dataIndex: 'spuName' },
    { title: '状态', slotName: 'statusCol', width: 100 },
    { title: '录入时间', dataIndex: 'createdAt' },
    { title: '操作', slotName: 'actions', align: 'right' as const, width: 180, fixed: 'right' as const },
]
</script>

<template>
    <div class="p-4 lg:p-6">
        <!-- 页面标题 -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
            <div>
                <h1 class="text-xl lg:text-2xl font-bold text-gray-800">SN码管理</h1>
                <p class="text-sm text-gray-500 mt-1">管理商品SN码</p>
            </div>
            <Space>
                <Button type="primary" @click="handleOpenAdd">
                    <template #icon>
                        <PlusIcon class="w-4 h-4"></PlusIcon>
                    </template>
                    新增SN码
                </Button>
                <Button type="primary" status="success" @click="router.push('/sn/scan')">
                    <template #icon>
                        <QrCodeIcon class="w-4 h-4"></QrCodeIcon>
                    </template>
                    扫码管理
                </Button>
                <Button @click="batchExportBarcode">
                    <template #icon>
                        <BarsArrowUpIcon class="w-4 h-4"></BarsArrowUpIcon>
                    </template>
                    批量打印二维码
                </Button>
            </Space>
        </div>

        <!-- 搜索区域 -->
        <Card class="mb-4">
            <Space direction="horizontal" :size="12" wrap>
                <Select v-model="selectedSkuCode" placeholder="按SKU编码筛选" style="width:260px" allow-clear
                    :loading="loadingSkus" filterable @change="handleAdvancedSearch">
                    <Select.Option v-for="s in skuList" :key="s.id" :value="s.skuCode">
                        {{ s.skuCode }}
                    </Select.Option>
                </Select>
                <Select v-model="searchForm.status" :options="statusOptions" placeholder="状态筛选" style="width:140px"
                    @change="handleAdvancedSearch"></Select>
                <DatePicker.RangePicker v-model="searchForm.dateRange" style="width:260px"
                    placeholder="['创建起始', '创建结束']" @change="handleAdvancedSearch" />
                <Button @click="handleReset">重置</Button>
            </Space>
        </Card>

        <!-- 数据表格 -->
        <Card>
            <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                    <Button size="small" :disabled="list.length === 0" @click="selectAllOnPage">
                        全选当前页（{{ list.length }}）
                    </Button>
                    <span v-if="selectedRowKeys.length > 0" class="text-sm text-blue-600">
                        已选中 {{ selectedRowKeys.length }} 条
                    </span>
                </div>
                <Button v-if="selectedRowKeys.length > 0" size="small" @click="clearSelection">清空选择</Button>
            </div>
            <Table :loading="loading" :columns="columns" :data="list" :pagination="false" :scroll="{ x: 1100 }"
                row-key="id" v-model:selected-keys="selectedRowKeys"
                :row-selection="{ type: 'checkbox', showCheckedAll: true, onlyCurrent: false, onChange: onSelectChange }">
                <template #statusCol="{ record }">
                    <Tag :color="(statusMap[record.status] || {}).color || 'gray'">
                        {{ (statusMap[record.status] || {}).label || '未知' }}
                    </Tag>
                </template>
                <template #actions="{ record }">
                    <Space>
                        <Button type="text" size="small" @click="handleOpenEdit(record)">编辑</Button>
                        <Button type="text" size="small" @click="openBarcodeModal(record.snCode)">查看二维码</Button>
                    </Space>
                </template>
            </Table>

            <!-- 分页 -->
            <div class="flex justify-end mt-4">
                <Pagination :current="query.page || 1" :total="total" :page-size="query.pageSize || 20"
                    :page-size-options="[10, 20, 50, 100]" show-total @change="handlePageChange"
                    @page-size-change="handlePageSizeChange"></Pagination>
            </div>
        </Card>
    </div>

    <SnAddModal v-model:visible="showAddModal" @saved="load" />
    <SnEditModal v-model:visible="showEditModal" :sn-id="editTarget.id" :current-status="editTarget.status"
        @saved="load" />

    <!-- 二维码预览弹窗 -->
    <Modal v-model:visible="showBarcodeModal" title="SN 码二维码" :footer="false" :width="400">
        <div class="flex flex-col items-center py-4">
            <canvas ref="barcodeCanvasRef"></canvas>
            <p class="text-gray-500 text-sm mt-2">{{ barcodeSn }}</p>
            <Button type="primary" class="mt-4" @click="downloadBarcodePng">
                下载 PNG
            </Button>
        </div>
    </Modal>
</template>