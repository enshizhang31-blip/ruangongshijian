package com.salemanager.modules.i18n.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 最小翻译单元 —— 一切可翻译内容的最小粒度
 */
@Data
@Document(collection = "translation_units")
@CompoundIndex(name = "idx_entity", def = "{'entityType':1, 'entityId':1, 'sortOrder':1}")
public class TranslationUnit {

    @Id
    private String id;

    @Indexed(unique = true)
    private String unitKey;

    private String entityType;
    private Long entityId;
    private String fieldPath;
    private String name;
    private String description;
    private String fieldType;
    private Integer sortOrder;
    private String baseLocale;
    private String baseContentHash;

    /** locale -> LocaleEntry */
    private Map<String, LocaleEntry> locales;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class LocaleEntry {
        private Object value;
        private String status;  // draft | translated | approved
        private LocalDateTime updatedAt;

        public LocaleEntry() {}

        public LocaleEntry(Object value, String status) {
            this.value = value;
            this.status = status;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
