/** 最小翻译单元 */
export interface TranslationUnit {
  id?: string
  unitKey: string
  entityType: string
  entityId: number
  fieldPath: string
  name: string
  description: string
  fieldType: FieldType
  sortOrder: number
  baseLocale: string
  locales: Record<string, LocaleEntry>
  createdAt?: string
  updatedAt?: string
}

export type FieldType = 'text' | 'rich_text' | 'number' | 'boolean' | 'array' | 'object'

export interface LocaleEntry {
  value: unknown
  status?: 'draft' | 'translated' | 'approved'
  updatedAt?: string
}

export interface UnitListResponse {
  entityType: string
  entityId: number
  units: TranslationUnit[]
}

export interface UnitPageResponse {
  items: TranslationUnit[]
  total: number
  page: number
  pageSize: number
}

export interface UnitStatusResponse {
  entityType: string
  entityId: number
  totalUnits: number
  localesStatus: Record<string, LocaleStatus>
}

export interface LocaleStatus {
  completeness: number
  outdated: number
  total: number
}

// 实体列表
export interface EntitySummary {
  entityType: string
  entityId: number
  name: string
}

export interface EntityListResponse {
  items: EntitySummary[]
  total: number
  page: number
  pageSize: number
}

// 实体字段详情
export interface EntityField {
  fieldPath: string
  fieldType: FieldType
  name: string
  description: string
  sortOrder: number
  unitKey: string
  hasMongo: boolean
  locales: Record<string, LocaleEntry>
}

export interface EntityFieldResponse {
  entityType: string
  entityId: number
  entityName: string
  fields: EntityField[]
}
