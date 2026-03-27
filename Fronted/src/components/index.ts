// ========== Base Components (Legacy) ==========
export { default as Table } from './Table.vue'
export { default as Pagination } from './Pagination.vue'
export { default as SearchBar } from './SearchBar.vue'
export { default as Modal } from './Modal.vue'

// ========== Common Components ==========
export * from './common'

// ========== Module Components ==========
export * from './product'
export * from './customer'
export * from './sale'
export * from './dashboard'

// ========== Arco Design Re-exports ==========
export {
    Table as ArcoTable,
    Button as ArcoButton,
    Input as ArcoInput,
    Form as ArcoForm,
    FormItem as ArcoFormItem,
    Card as ArcoCard,
    Space as ArcoSpace,
    Tag as ArcoTag,
    Avatar as ArcoAvatar,
    Dropdown as ArcoDropdown,
    Menu as ArcoMenu,
    MenuItem as ArcoMenuItem,
    Row as ArcoRow,
    Col as ArcoCol,
    Statistic as ArcoStatistic,
    Skeleton as ArcoSkeleton,
    Popconfirm as ArcoPopconfirm,
    Message as ArcoMessage,
} from '@arco-design/web-vue'
