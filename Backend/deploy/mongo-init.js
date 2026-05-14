// =====================================================
// MongoDB 初始化脚本 - 国际化翻译数据
// Docker 首次启动时自动执行
// =====================================================

db = db.getSiblingDB('sale_manager_i18n');

// 创建集合
db.createCollection('translation_units');

// 创建索引
db.translation_units.createIndex({ unit_key: 1 }, { unique: true, name: 'idx_unit_key' });
db.translation_units.createIndex({ entity_type: 1, entity_id: 1, sort_order: 1 }, { name: 'idx_entity' });
db.translation_units.createIndex({ 'entity_type': 1, 'locales.zh-CN.status': 1 }, { name: 'idx_status' });
db.translation_units.createIndex({ 'locales.zh-CN.value': 'text' }, { name: 'idx_text_zh' });
db.translation_units.createIndex({ 'locales.en-US.value': 'text' }, { name: 'idx_text_en' });
db.translation_units.createIndex({ updated_at: 1 }, { name: 'idx_updated_at' });

print('MongoDB: sale_manager_i18n.translation_units initialized with indexes.');
