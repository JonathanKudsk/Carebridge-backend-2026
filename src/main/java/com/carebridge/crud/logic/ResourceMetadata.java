package com.carebridge.crud.logic;

import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.crud.logic.core.CrudInterceptor;

import java.util.List;
import java.util.Map;

/**
 * [LOGIC LAYER]
 * Holds metadata about a CRUD resource.
 */
public class ResourceMetadata<T extends BaseEntity> {
    private final Class<T>              entityClass;
    private final String                basePath;
    private final GenericRepository<T>  repository;
    private final BaseService<T>        service;
    private final CrudInterceptor<T>    interceptor;
    private final List<FieldInfo>       fields;

    private ResourceMetadata(Builder<T> builder) {
        this.entityClass =  builder.entityClass;
        this.basePath =     builder.basePath;
        this.repository =   builder.repository;
        this.service =      builder.service;
        this.interceptor =  builder.interceptor;
        this.fields =       builder.fields;
    }

    public static <T extends BaseEntity> Builder<T> builder() {
        return new Builder<>();
    }

    public Class<T> getEntityClass()            { return entityClass; }
    public String getBasePath()                 { return basePath; }
    public GenericRepository<T> getRepository() { return repository; }
    public BaseService<T> getService()          { return service; }
    public CrudInterceptor<T> getInterceptor()  { return interceptor; }
    public List<FieldInfo> getFields()          { return fields; }

    public static class FieldInfo {
        private final String                name;
        private final String                type;
        private final boolean               required;
        private final Map<String, Object>   constraints;

        public FieldInfo
        (
            String                  name, 
            String                  type,
            boolean                 required, 
            Map<String, Object>     constraints
        ) {
            this.name =             name;
            this.type =             type;
            this.required =         required;
            this.constraints =      constraints;
        }

        public String getName()                     { return name; }
        public String getType()                     { return type; }
        public boolean isRequired()                 { return required; }
        public Map<String, Object> getConstraints() { return constraints; }
    }

    public static class Builder<T extends BaseEntity> {
        private Class<T>                entityClass;
        private String                  basePath;
        private GenericRepository<T>    repository;
        private BaseService<T>          service;
        private CrudInterceptor<T>      interceptor;
        private List<FieldInfo>         fields;

        public Builder<T> entityClass(Class<T> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder<T> basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder<T> repository(GenericRepository<T> repository) {
            this.repository = repository;
            return this;
        }

        public Builder<T> service(BaseService<T> service) {
            this.service = service;
            return this;
        }

        public Builder<T> interceptor(CrudInterceptor<T> interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        public Builder<T> fields(List<FieldInfo> fields) {
            this.fields = fields;
            return this;
        }

        public ResourceMetadata<T> build() {
            return new ResourceMetadata<>(this);
        }
    }
}
