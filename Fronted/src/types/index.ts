// ========== 通用 ==========
export type { PageQuery, PageResult, Result } from './common'

// ========== 用户/员工 ==========
export type { AdminUser, Department, Role } from './user'

// ========== 商品 ==========
export type { Product, ProductCategory, Sku, Spec, SpecValue, BatchGenerateSkuParams } from './product'

// ========== 客户 ==========
export type { Customer } from './customer'

// ========== 销售 ==========
export type { SaleOrder, SaleOrderItem } from './sale'

// ========== 统计 ==========
export type { DashboardStats } from './statistics'

// ========== SN码 ==========
export type { SnCode, SnCodeLog, SnQuery } from './sn'

// ========== 国际化 ==========
export type { TranslationUnit, FieldType, LocaleEntry, UnitListResponse, UnitStatusResponse, LocaleStatus } from './i18n'
