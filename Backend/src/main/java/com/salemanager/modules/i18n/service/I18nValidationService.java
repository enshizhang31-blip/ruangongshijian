package com.salemanager.modules.i18n.service;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.i18n.model.TranslationUnit;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class I18nValidationService {

    private static final List<String> VALID_FIELD_TYPES = List.of("text", "rich_text", "number", "boolean", "array", "object");

    /** 校验 locale value 是否符合 field_type */
    public void validateValue(TranslationUnit unit, String locale, Object value) {
        String fieldType = unit.getFieldType();
        if (!VALID_FIELD_TYPES.contains(fieldType)) {
            throw new BusinessException(400, "TYPE_MISMATCH", "未知的 field_type: " + fieldType);
        }

        if (value == null) {
            throw new BusinessException(400, "VALUE_REQUIRED", "翻译值不能为空");
        }

        switch (fieldType) {
            case "text":
                if (!(value instanceof String s) || s.length() > 500) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "text 类型要求字符串，最大 500 字符");
                }
                break;
            case "rich_text":
                if (!(value instanceof String s) || s.length() > 50000) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "rich_text 类型要求字符串，最大 50000 字符");
                }
                break;
            case "number":
                if (!(value instanceof Number)) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "number 类型要求数字");
                }
                // 跨语言一致性：所有 locale 的 number 必须相等
                validateConsistentNumber(unit, value);
                break;
            case "boolean":
                if (!(value instanceof Boolean)) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "boolean 类型要求布尔值");
                }
                validateConsistentBoolean(unit, value);
                break;
            case "array":
                if (!(value instanceof List)) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "array 类型要求数组");
                }
                List<?> list = (List<?>) value;
                if (list.size() > 100) {
                    throw new BusinessException(400, "ARRAY_TOO_LARGE", "数组最多 100 项");
                }
                for (int i = 0; i < list.size(); i++) {
                    if (!(list.get(i) instanceof String)) {
                        throw new BusinessException(400, "ARRAY_ELEMENT_TYPE", "数组元素必须是字符串");
                    }
                }
                break;
            case "object":
                if (!(value instanceof java.util.Map || value instanceof List)) {
                    throw new BusinessException(400, "TYPE_MISMATCH", "object 类型要求 JSON Object 或 Array");
                }
                break;
        }
    }

    /** 校验基准语言的 value 不为空 */
    public void validateBaseValue(TranslationUnit unit) {
        String baseLocale = unit.getBaseLocale();
        TranslationUnit.LocaleEntry base = unit.getLocales().get(baseLocale);
        if (base == null || base.getValue() == null || (base.getValue() instanceof String s && !StringUtils.hasText(s))) {
            throw new BusinessException(400, "VALUE_REQUIRED", "基准语言 (" + baseLocale + ") 的值不能为空");
        }
    }

    /** 校验是否可覆盖已审批的翻译 */
    public void checkOverwriteAllowed(TranslationUnit unit, String locale, boolean force) {
        TranslationUnit.LocaleEntry entry = unit.getLocales().get(locale);
        if (entry != null && "approved".equals(entry.getStatus()) && !force) {
            throw new BusinessException(400, "OVERWRITE_DENIED", "该翻译已审批，如需覆盖请设置 force=true");
        }
    }

    private void validateConsistentNumber(TranslationUnit unit, Object newValue) {
        for (var entry : unit.getLocales().entrySet()) {
            Object existing = entry.getValue().getValue();
            if (existing instanceof Number n && !n.equals(newValue)) {
                throw new BusinessException(400, "VALUE_DIVERGED",
                    String.format("number 类型在所有语言中必须一致，locale '%s' 已有值 %s", entry.getKey(), n));
            }
        }
    }

    private void validateConsistentBoolean(TranslationUnit unit, Object newValue) {
        for (var entry : unit.getLocales().entrySet()) {
            Object existing = entry.getValue().getValue();
            if (existing instanceof Boolean b && !b.equals(newValue)) {
                throw new BusinessException(400, "VALUE_DIVERGED",
                    String.format("boolean 类型在所有语言中必须一致，locale '%s' 已有值 %s", entry.getKey(), b));
            }
        }
    }
}
