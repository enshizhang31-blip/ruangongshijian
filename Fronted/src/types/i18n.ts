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
  status: 'draft' | 'translated' | 'approved'
  updatedAt?: string
}

export interface UnitListResponse {
  entityType: string
  entityId: number
  units: TranslationUnit[]
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
