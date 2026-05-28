import api from './request'
import type { SnCode, SnCodeLog, SnQuery, PageQuery, PageResult } from '@/types'

export const snApi = {
    list(params: PageQuery & SnQuery) {
        return api.get<PageResult<SnCode>>('/admin/sn/code', { params })
    },
    getById(id: number) {
        return api.get<SnCode>(`/admin/sn/code/${id}`)
    },
    create(data: { sn: string; goodsId: number; remark?: string }) {
        return api.post<SnCode>('/admin/sn/code', data)
    },
    batchCreate(data: { sns: string[]; goodsId: number; remark?: string }) {
        return api.post<{ success: number; failed: number }>('/admin/sn/code/batch', data)
    },
    bind(data: { sn: string; orderId: number }) {
        return api.post<void>('/admin/sn/code/bind', data)
    },
    unbind(id: number) {
        return api.post<void>(`/admin/sn/code/${id}/unbind`)
    },
    query(sn: string) {
        return api.get<SnCode>(`/admin/sn/code/query/${sn}`)
    },
    logs(params: PageQuery & { snId?: number }) {
        return api.get<PageResult<SnCodeLog>>('/admin/sn/log', { params })
    },
    getByGoodsId(goodsId: number) {
        return api.get<SnCode[]>(`/admin/sn/code/goods/${goodsId}`)
    },
    // 按SKU ID获取SN码列表
    getBySkuId(skuId: number, params?: PageQuery) {
        return api.get<PageResult<SnCode>>(`/admin/sn/code/sku/${skuId}`, { params })
    },
    // 获取SKU的SN码状态统计
    getStatsBySkuId(skuId: number) {
        return api.get<Record<string, number>>(`/admin/sn/code/sku/${skuId}/stats`)
    },
    // 作废SN码
    voidCode(id: number, remark?: string) {
        return api.post<void>(`/admin/sn/code/${id}/void`, { remark })
    },
    // 退货申请
    applyReturn(id: number, remark?: string) {
        return api.post<void>(`/admin/sn/code/${id}/return`, { remark })
    },
    // 退货完成
    completeReturn(id: number, remark?: string) {
        return api.post<void>(`/admin/sn/code/${id}/return-complete`, { remark })
    },
    // 自动生成SN码（基于SKU编码+序号后缀）
    generate(skuId: number, count: number = 1) {
        return api.post<SnCode[]>('/admin/sn/code/generate', { skuId, count })
    },
    // 更新SN码状态
    updateStatus(id: number, status: number) {
        return api.put<void>(`/admin/sn/code/${id}/status`, { status })
    },
}
