import { api } from './request'
import type { TranslationUnit, UnitListResponse, UnitStatusResponse, UnitPageResponse, EntityListResponse, EntityFieldResponse } from '@/types/i18n'

export const i18nApi = {
  /** 获取实体所有最小元 */
  getUnits(entityType: string, entityId: number) {
    return api.get<UnitListResponse>('/admin/i18n/units', { params: { entityType, entityId } })
  },

  /** 分页查询翻译单元 */
  getUnitsPaged(params: { entityType?: string; keyword?: string; page?: number; pageSize?: number }) {
    return api.get<UnitPageResponse>('/admin/i18n/units/list', { params })
  },

  /** 搜索实体（从MySQL） */
  searchEntities(params: { entityType?: string; keyword?: string; page?: number; pageSize?: number }) {
    return api.get<EntityListResponse>('/admin/i18n/entities', { params })
  },

  /** 获取实体字段详情（MySQL + MongoDB合并） */
  getEntityFields(entityType: string, entityId: number) {
    return api.get<EntityFieldResponse>(`/admin/i18n/entities/${entityType}/${entityId}`)
  },

  /** 获取支持的语言列表 */
  getLocales() {
    return api.get<string[]>('/admin/i18n/locales')
  },

  /** 保存单个最小元(单语言) */
  saveUnit(unitKey: string, locale: string, value: unknown, force = false) {
    return api.put<{ success: boolean }>(`/admin/i18n/units/${encodeURIComponent(unitKey)}`, { locale, value, force })
  },

  /** 批量保存 */
  batchSave(entityType: string, entityId: number, units: { unitKey: string; locales: Record<string, { value: unknown; status?: string }> }[]) {
    return api.put<{ success: boolean; count: number }>('/admin/i18n/units/batch', { entityType, entityId, units })
  },

  /** 删除实体所有翻译 */
  deleteUnits(entityType: string, entityId: number) {
    return api.delete<{ success: boolean }>('/admin/i18n/units', { params: { entityType, entityId } })
  },

  /** 获取翻译状态 */
  getStatus(entityType: string, entityId: number) {
    return api.get<UnitStatusResponse>('/admin/i18n/units/status', { params: { entityType, entityId } })
  },
}
