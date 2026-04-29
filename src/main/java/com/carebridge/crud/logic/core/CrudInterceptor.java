package com.carebridge.crud.logic.core;

import com.carebridge.crud.data.core.BaseEntity;

/**
 * [LOGIC LAYER]
 * Interceptor for CRUD operations.
 */
public interface CrudInterceptor<T extends BaseEntity> {
    default void beforeCreate(T entity) {}
    default void afterCreate(T entity) {}
    default void beforeUpdate(T entity) {}
    default void afterUpdate(T entity) {}
    default void beforeDelete(Long id) {}
    default void afterDelete(Long id) {}
}
