package com.salemanager.modules.i18n.repository;

import com.salemanager.modules.i18n.model.TranslationUnit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TranslationUnitRepository extends MongoRepository<TranslationUnit, String> {

    TranslationUnit findByUnitKey(String unitKey);

    List<TranslationUnit> findByEntityTypeAndEntityIdOrderBySortOrderAsc(String entityType, Long entityId);

    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
