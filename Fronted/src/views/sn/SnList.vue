<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { snApi, productApi } from '@/api'
import { usePageQuery } from '@/composables'
import { Table, Button, Space, Tag, Card, Select, Message, Pagination, Modal, DatePicker, Radio, RadioGroup } from '@arco-design/web-vue'
import { PlusIcon, BarsArrowUpIcon, QrCodeIcon } from '@heroicons/vue/24/outline'
import { useRouter } from 'vue-router'
import SnAddModal from '@/components/SnAddModal.vue'
import SnEditModal from '@/components/SnEditModal.vue'
import type { SnCode, Sku } from '@/types'

const { loading, list, total, query, load, setPage, setKeyword, setPageSize } = usePageQuery(snApi.list)
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
        ...pageRows.filter((r: any) => !existingIds.has(r.id)),
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
    source: undefined as number | undefined,
    dateRange: [] as string[],
})

const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '在库', color: 'orange' },
    1: { label: '已售', color: 'arcoblue' },
    2: { label: '已作废', color: 'gray' },
    3: { label: '退货中', color: 'purple' },
    4: { label: '已退货', color: 'red' },
}

const statusOptions = [
    { label: '全部状态', value: undefined },
    { label: '在库', value: 0 },
    { label: '已售', value: 1 },
    { label: '已作废', value: 2 },
    { label: '退货中', value: 3 },
    { label: '已退货', value: 4 },
]

const sourceOptions = [
    { label: '全部来源', value: undefined },
    { label: '手动录入', value: 1 },
    { label: 'CSV 导入', value: 2 },
    { label: '自动生成', value: 3 },
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
        source: searchForm.source,
        startDate: searchForm.dateRange?.[0] || undefined,
        endDate: searchForm.dateRange?.[1] || undefined,
    }
    clearSelection()
    load()
}
function handleReset() {
    selectedSkuCode.value = ''
    searchForm.status = undefined
    searchForm.source = undefined
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

// ============ 二维码预览（单个） ============
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

// ============ 批量导出范围选择 ============
const showExportPicker = ref(false)
const exportRange = ref<'selected' | 'filtered' | 'all'>('selected')
const exportLoading = ref(false)

function openExportPicker() {
    if (selectedRows.value.length === 0 && list.value.length === 0) {
        Message.warning('当前列表为空，请先筛选数据')
        return
    }
    exportRange.value = selectedRows.value.length > 0 ? 'selected' : 'filtered'
    showExportPicker.value = true
}

const exportSummary = computed(() => {
    if (exportRange.value === 'selected') {
        return { count: selectedRows.value.length, desc: '已选中的 SN 码' }
    }
    if (exportRange.value === 'filtered') {
        return { count: list.value.length, desc: '当前筛选条件下的 SN 码（仅当前页）' }
    }
    return { count: total.value, desc: '当前筛选条件下的全部 SN 码（包含其他页）' }
})

async function confirmExport() {
    if (exportRange.value === 'selected') {
        if (selectedRows.value.length === 0) {
            Message.warning('请先勾选要导出的 SN 码')
            return
        }
        showExportPicker.value = false
        batchExportBarcode(selectedRows.value)
        return
    }
    if (exportRange.value === 'filtered') {
        showExportPicker.value = false
        batchExportBarcode(list.value)
        return
    }
    exportLoading.value = true
    try {
        const params: any = {
            page: 1,
            pageSize: Math.max(total.value, 1),
            keyword: selectedSkuCode.value || undefined,
            status: searchForm.status,
            source: searchForm.source,
            startDate: searchForm.dateRange?.[0] || undefined,
            endDate: searchForm.dateRange?.[1] || undefined,
        }
        const res: any = await snApi.list(params)
        const allRows: SnCode[] = res.list || res.records || []
        if (allRows.length === 0) {
            Message.warning('没有可导出的 SN 码')
            return
        }
        if (allRows.length > 5000) {
            Message.warning(`数据量过大（${allRows.length} 条），建议缩小筛选范围或分批导出`)
            return
        }
        showExportPicker.value = false
        batchExportBarcode(allRows)
    } catch (e) {
        console.error('拉取全部 SN 码失败', e)
        Message.error('拉取失败，请稍后重试')
    } finally {
        exportLoading.value = false
    }
}

// 批量导出二维码（在新窗口打开打印页面）
function batchExportBarcode(rows: SnCode[]) {
    if (!rows || rows.length === 0) {
        Message.warning('没有可导出的数据')
        return
    }
    const printWindow = window.open('', '_blank')
    if (!printWindow) {
        Message.error('请允许弹窗以打开打印页面')
        return
    }
    Message.info('正在打开打印窗口...')
    const exportItems = rows.map((item: any) => ({
        sn: item.snCode || item.sn || '',
        name: item.spuName || '',
        code: item.skuCode || '',
        status: item.status ?? 0,
    }))
    writePrintPage(printWindow, exportItems)
}

function writePrintPage(printWindow: Window, exportItems: any[]) {
    const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>SN码二维码打印</title>
      <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"
              onerror="window.__qrcodeLoadFailed=true"><\/script>
      <style>
        body { margin: 0; padding: 20px; font-family: Arial, sans-serif; background: #fafafa; }
        .toolbar { margin-bottom: 16px; padding: 12px; background: #fff; border-radius: 6px;
                   box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
        .toolbar button { padding: 8px 18px; font-size: 14px; background: #165dff; color: #fff;
                          border: none; border-radius: 4px; cursor: pointer; margin-right: 8px; }
        .toolbar button.secondary { background: #fff; color: #165dff; border: 1px solid #165dff; }
        .toolbar .info { color: #666; font-size: 13px; margin-left: 8px; }
        .toolbar .warn { color: #f5222d; font-size: 13px; margin-top: 8px; display: none; }
        .qrcode-grid { display: flex; flex-wrap: wrap; gap: 12px; }
        .qrcode-item {
          display: inline-block;
          text-align: center;
          margin: 6px;
          padding: 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          background: #fff;
          vertical-align: top;
          width: 200px;
          box-sizing: border-box;
        }
        .qrcode-item .name {
          font-size: 12px;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-weight: 600;
        }
        .qrcode-item .code {
          font-size: 11px;
          color: #999;
          margin-bottom: 6px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .qrcode-item canvas {
          display: block;
          margin: 0 auto;
        }
        .qrcode-item .placeholder {
          width: 160px; height: 160px; line-height: 160px;
          background: #f5f5f5; color: #999; font-size: 12px;
          border: 1px dashed #ccc;
        }
        .qrcode-item .sn {
          font-size: 11px;
          color: #666;
          margin-top: 6px;
          font-family: 'Courier New', monospace;
          word-break: break-all;
          max-width: 180px;
        }
        .qrcode-item .status {
          display: inline-block;
          font-size: 10px;
          padding: 2px 8px;
          border-radius: 10px;
          background: #52c41a;
          color: #fff;
          margin-top: 6px;
        }
        .qrcode-item .status.s0 { background: #faad14; }
        .qrcode-item .status.s1 { background: #165dff; }
        .qrcode-item .status.s2 { background: #999; }
        .qrcode-item .status.s3 { background: #722ed1; }
        .qrcode-item .status.s4 { background: #f5222d; }
        @media print {
          .toolbar { display: none; }
          body { padding: 0; background: #fff; }
          .qrcode-item { page-break-inside: avoid; box-shadow: none; }
        }
      </style>
    </head>
    <body>
      <div class="toolbar">
        <button onclick="window.print()">🖨️ 打印二维码</button>
        <button class="secondary" onclick="window.close()">关闭</button>
        <span class="info">共 ${exportItems.length} 个 SN 码 · 标签尺寸建议: 50mm × 30mm</span>
        <div class="warn" id="warn">⚠️ 二维码库加载失败，请检查网络。可截屏保存下方 SN 码文本。</div>
      </div>
      <div class="qrcode-grid" id="container"></div>
      <script>
        const statusNameMap = { 0: '在库', 1: '已售', 2: '已作废', 3: '退货中', 4: '已退货' };
        const items = ${JSON.stringify(exportItems)};
        const container = document.getElementById('container');
        const warnEl = document.getElementById('warn');

        function renderOne(item, idx) {
          const div = document.createElement('div');
          div.className = 'qrcode-item';

          const nameDiv = document.createElement('div');
          nameDiv.className = 'name';
          nameDiv.textContent = item.name || '未命名商品';
          div.appendChild(nameDiv);

          if (item.code) {
            const codeDiv = document.createElement('div');
            codeDiv.className = 'code';
            codeDiv.textContent = item.code;
            div.appendChild(codeDiv);
          }

          const canvas = document.createElement('canvas');
          canvas.id = 'qr' + idx;
          canvas.width = 160;
          canvas.height = 160;
          div.appendChild(canvas);

          const snDiv = document.createElement('div');
          snDiv.className = 'sn';
          snDiv.textContent = item.sn || '(空 SN)';
          div.appendChild(snDiv);

          const statusDiv = document.createElement('div');
          statusDiv.className = 'status s' + (item.status ?? 0);
          statusDiv.textContent = statusNameMap[item.status] || '在库';
          div.appendChild(statusDiv);

          container.appendChild(div);

          if (!item.sn) {
            canvas.outerHTML = '<div class="placeholder">SN 为空</div>';
            return;
          }
          if (typeof QRCode === 'undefined') {
            canvas.outerHTML = '<div class="placeholder">二维码库未加载</div>';
            return;
          }
          try {
            QRCode.toCanvas(canvas, item.sn, {
              width: 160,
              margin: 1,
              errorCorrectionLevel: 'M',
              color: { dark: '#000000', light: '#ffffff' }
            }, function (err) {
              if (err) {
                console.error('生成失败', item.sn, err);
                canvas.outerHTML = '<div class="placeholder">生成失败</div>';
              }
            });
          } catch (e) {
            console.error(e);
            canvas.outerHTML = '<div class="placeholder">生成失败</div>';
          }
        }

        setTimeout(function() {
          if (typeof QRCode === 'undefined' || window.__qrcodeLoadFailed) {
            warnEl.style.display = 'block';
          }
        }, 2000);

        const BATCH = 50;
        let i = 0;
        function next() {
          const end = Math.min(i + BATCH, items.length);
          for (; i < end; i++) renderOne(items[i], i);
          if (i < items.length) {
            requestAnimationFrame(next);
          }
        }
        next();
      <\/script>
    </body>
    </html>
    `
    printWindow.document.write(html)
    printWindow.document.close()
    Message.success(`已生成 ${exportItems.length} 个二维码`)
}

const columns = [
    { title: 'ID', dataIndex: 'id', width: 80, fixed: 'left' as const },
    { title: 'SN码', dataIndex: 'snCode', width: 180 },
    { title: '商品名称', dataIndex: 'spuName' },
    { title: '状态', slotName: 'statusCol', width: 100 },
    { title: '销售时间', dataIndex: 'soldAt' },
    { title: '创建时间', dataIndex: 'createdAt' },
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
                <Button @click="openExportPicker">
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
                <Select v-model="searchForm.source" :options="sourceOptions" placeholder="录入方式" style="width:140px"
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

    <!-- 单个二维码预览弹窗 -->
    <Modal v-model:visible="showBarcodeModal" title="SN 码二维码" :footer="false" :width="400">
        <div class="flex flex-col items-center py-4">
            <canvas ref="barcodeCanvasRef"></canvas>
            <p class="text-gray-500 text-sm mt-2">{{ barcodeSn }}</p>
            <Button type="primary" class="mt-4" @click="downloadBarcodePng">
                下载 PNG
            </Button>
        </div>
    </Modal>

    <!-- 导出范围选择弹窗 -->
    <Modal v-model:visible="showExportPicker" title="批量打印二维码" :ok-text="'开始导出'"
        :ok-button-props="{ loading: exportLoading }" @ok="confirmExport">
        <div class="py-2">
            <p class="text-sm text-gray-600 mb-4">请选择要导出的范围：</p>
            <RadioGroup v-model="exportRange" direction="vertical">
                <Radio value="selected" :disabled="selectedRows.length === 0">
                    <span class="font-medium">已选中的 SN 码</span>
                    <span class="text-gray-500 text-xs ml-2">({{ selectedRows.length }} 条)</span>
                    <span v-if="selectedRows.length === 0" class="text-red-500 text-xs ml-2">请先在表格中勾选</span>
                </Radio>
                <Radio value="filtered">
                    <span class="font-medium">当前筛选条件下的 SN 码</span>
                    <span class="text-gray-500 text-xs ml-2">(当前页 {{ list.length }} 条)</span>
                </Radio>
                <Radio value="all">
                    <span class="font-medium">当前筛选条件下的全部 SN 码</span>
                    <span class="text-gray-500 text-xs ml-2">(共 {{ total }} 条，含其他页)</span>
                </Radio>
            </RadioGroup>
            <div class="mt-4 p-3 bg-blue-50 rounded text-sm text-blue-700">
                将要导出：<b>{{ exportSummary.count }}</b> 条 · {{ exportSummary.desc }}
            </div>
        </div>
    </Modal>
</template>