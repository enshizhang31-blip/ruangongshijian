package com.salemanager.modules.i18n.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BatchSaveParam {

    @NotBlank(message = "实体类型不能为空")
    private String entityType;

    @NotNull(message = "实体ID不能为空")
    @Positive(message = "实体ID必须为正数")
    private Long entityId;

    private List<UnitEntry> units;

    @Data
    public static class UnitEntry {
        @NotBlank(message = "unitKey不能为空")
        private String unitKey;
        /** locale -> { value, status } */
        private Map<String, LocaleValue> locales;
    }

    @Data
    public static class LocaleValue {
        private Object value;
        private String status;
    }
}
