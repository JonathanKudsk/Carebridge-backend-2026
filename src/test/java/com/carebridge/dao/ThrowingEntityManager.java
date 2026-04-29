package com.carebridge.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A stub EntityManager that throws exceptions for all query/persist methods
 * to help reach 100% coverage in DAOs.
 */
public class ThrowingEntityManager implements EntityManager {
    
    public static class ThrowingQuery<T> implements TypedQuery<T> {
        @Override public List<T> getResultList() { throw new RuntimeException("Stub Error"); }
        @Override public T getSingleResult() { throw new NoResultException("Stub No Result"); }
        @Override public int executeUpdate() { return 0; }
        @Override public TypedQuery<T> setMaxResults(int maxResults) { return this; }
        @Override public int getMaxResults() { return 0; }
        @Override public TypedQuery<T> setFirstResult(int startPosition) { return this; }
        @Override public int getFirstResult() { return 0; }
        @Override public TypedQuery<T> setHint(String hintName, Object value) { return this; }
        @Override public Map<String, Object> getHints() { return null; }
        @Override public <T1> TypedQuery<T> setParameter(Parameter<T1> param, T1 value) { return this; }
        @Override public TypedQuery<T> setParameter(Parameter<java.util.Calendar> param, java.util.Calendar value, TemporalType temporalType) { return this; }
        @Override public TypedQuery<T> setParameter(Parameter<java.util.Date> param, java.util.Date value, TemporalType temporalType) { return this; }
        @Override public TypedQuery<T> setParameter(String name, Object value) { return this; }
        @Override public TypedQuery<T> setParameter(String name, java.util.Calendar value, TemporalType temporalType) { return this; }
        @Override public TypedQuery<T> setParameter(String name, java.util.Date value, TemporalType temporalType) { return this; }
        @Override public TypedQuery<T> setParameter(int position, Object value) { return this; }
        @Override public TypedQuery<T> setParameter(int position, java.util.Calendar value, TemporalType temporalType) { return this; }
        @Override public TypedQuery<T> setParameter(int position, java.util.Date value, TemporalType temporalType) { return this; }
        @Override public Set<Parameter<?>> getParameters() { return null; }
        @Override public Parameter<?> getParameter(String name) { return null; }
        @Override public <T1> Parameter<T1> getParameter(String name, Class<T1> type) { return null; }
        @Override public Parameter<?> getParameter(int position) { return null; }
        @Override public <T1> Parameter<T1> getParameter(int position, Class<T1> type) { return null; }
        @Override public boolean isBound(Parameter<?> param) { return false; }
        @Override public <T1> T1 getParameterValue(Parameter<T1> param) { return null; }
        @Override public Object getParameterValue(String name) { return null; }
        @Override public Object getParameterValue(int position) { return null; }
        @Override public TypedQuery<T> setFlushMode(FlushModeType flushMode) { return this; }
        @Override public FlushModeType getFlushMode() { return null; }
        @Override public TypedQuery<T> setLockMode(LockModeType lockMode) { return this; }
        @Override public LockModeType getLockMode() { return null; }
        @Override public <T1> T1 unwrap(Class<T1> cls) { return null; }
    }

    @Override public void persist(Object entity) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T merge(T entity) { throw new RuntimeException("Stub Error"); }
    @Override public void remove(Object entity) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T find(Class<T> entityClass, Object primaryKey) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) { throw new RuntimeException("Stub Error"); }
    @Override public <T> T getReference(Class<T> entityClass, Object primaryKey) { throw new RuntimeException("Stub Error"); }
    @Override public void flush() { throw new RuntimeException("Stub Error"); }
    @Override public void setFlushMode(FlushModeType flushMode) {}
    @Override public FlushModeType getFlushMode() { return null; }
    @Override public void lock(Object entity, LockModeType lockMode) {}
    @Override public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {}
    @Override public void refresh(Object entity) {}
    @Override public void refresh(Object entity, Map<String, Object> properties) {}
    @Override public void refresh(Object entity, LockModeType lockMode) {}
    @Override public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {}
    @Override public void clear() {}
    @Override public void detach(Object entity) {}
    @Override public boolean contains(Object entity) { return false; }
    @Override public LockModeType getLockMode(Object entity) { return null; }
    @Override public void setProperty(String propertyName, Object value) {}
    @Override public Map<String, Object> getProperties() { return null; }
    @Override public Query createQuery(String qlString) { return new ThrowingQuery<>(); }
    @Override public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) { return new ThrowingQuery<>(); }
    @Override public Query createQuery(CriteriaUpdate updateQuery) { throw new RuntimeException("Stub Error"); }
    @Override public Query createQuery(CriteriaDelete deleteQuery) { throw new RuntimeException("Stub Error"); }
    @Override public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) { return new ThrowingQuery<>(); }
    @Override public Query createNamedQuery(String name) { return new ThrowingQuery<>(); }
    @Override public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) { return new ThrowingQuery<>(); }
    @Override public Query createNativeQuery(String sqlString) { throw new RuntimeException("Stub Error"); }
    @Override public Query createNativeQuery(String sqlString, Class resultClass) { throw new RuntimeException("Stub Error"); }
    @Override public Query createNativeQuery(String sqlString, String resultSetMapping) { throw new RuntimeException("Stub Error"); }
    @Override public StoredProcedureQuery createNamedStoredProcedureQuery(String name) { return null; }
    @Override public StoredProcedureQuery createStoredProcedureQuery(String procedureName) { return null; }
    @Override public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) { return null; }
    @Override public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) { return null; }
    @Override public void joinTransaction() {}
    @Override public boolean isJoinedToTransaction() { return false; }
    @Override public <T> T unwrap(Class<T> cls) { return null; }
    @Override public Object getDelegate() { return null; }
    @Override public void close() {}
    @Override public boolean isOpen() { return true; }
    @Override public EntityTransaction getTransaction() { return null; }
    @Override public EntityManagerFactory getEntityManagerFactory() { return null; }
    @Override public CriteriaBuilder getCriteriaBuilder() { throw new RuntimeException("Stub Error"); }
    @Override public Metamodel getMetamodel() { return null; }
    @Override public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) { return null; }
    @Override public EntityGraph<?> createEntityGraph(String graphName) { return null; }
    @Override public EntityGraph<?> getEntityGraph(String graphName) { return null; }
    @Override public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) { return null; }
}
