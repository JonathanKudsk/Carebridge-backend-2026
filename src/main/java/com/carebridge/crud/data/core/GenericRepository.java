package com.carebridge.crud.data.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * [DATA LAYER]
 * A universal DAO that communicates with Hibernate via EntityManager.
 */
public class GenericRepository<T extends BaseEntity> {
    
    private final EntityManager entityManager;
    private final Class<T> entityClass;

    public GenericRepository(Class<T> entityClass, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.entityManager = entityManager;
    }

    public List<T> findAll() {
        return entityManager.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }

    public List<T> findAll(int page, int size) {
        TypedQuery<T> query = entityManager.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Transactional
    public T save(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Transactional
    public void deleteById(Long id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public long count() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(entityClass)));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
