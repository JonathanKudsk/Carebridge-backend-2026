package com.carebridge.crud.logic.core;

import com.carebridge.crud.data.core.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * [LOGIC LAYER]
 * A universal service that uses EntityManager directly to perform generic CRUD.
 */
public class BaseService<T extends BaseEntity> {
    
    protected final EntityManager em;
    protected final Class<T> entityClass;

    public BaseService(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    public List<T> findAll() {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }

    public Page<T> findAll(int page, int size) {
        TypedQuery<T> query = em.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        
        List<T> content = query.getResultList();
        
        long totalElements = em.createQuery("SELECT count(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
                
        return new Page<>(content, totalElements);
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Transactional
    public T save(T entity) {
        if (entity.getId() != null) {
            return update(entity.getId(), entity); 
        }
        em.persist(entity);
        return entity;
    }

    @Transactional
    public T update(Long id, T entity) {
        T existing = em.find(entityClass, id);
        if (existing == null) {
            throw new RuntimeException("Entity not found with id: " + id);
        }
        entity.setId(id);
        return em.merge(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public static class Page<T> {
        private final List<T> content;
        private final long totalElements;

        public Page(List<T> content, long totalElements) {
            this.content = content;
            this.totalElements = totalElements;
        }

        public List<T> getContent() { return content; }
        public long getTotalElements() { return totalElements; }
    }
}
