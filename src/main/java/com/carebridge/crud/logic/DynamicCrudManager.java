package com.carebridge.crud.logic;

import com.carebridge.crud.annotations.CrudResource;
import com.carebridge.crud.annotations.ExcludeFromMeta;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.crud.logic.core.CrudInterceptor;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * [LOGIC LAYER]
 * The heart of the Universal CRUD system. Discovers entities, builds metadata,
 * and initializes universal DAOs/Services.
 */
@Component
public class DynamicCrudManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicCrudManager.class);
    private final Map<String, ResourceMetadata<?>> resources = new HashMap<>();
    private final Map<Class<?>, CrudInterceptor<?>> interceptors = new HashMap<>();
    
    @PersistenceContext
    private EntityManager entityManager;

    public DynamicCrudManager() {
    }

    public void registerInterceptor(Class<?> entityClass, CrudInterceptor<?> interceptor) {
        interceptors.put(entityClass, interceptor);
    }

    @PostConstruct
    public void init() {
        discoverAndRegister("com.carebridge.entities");
    }

    /**
     * Scans a package for @CrudResource entities and registers them.
     */
    public void discoverAndRegister(String packageName) {
        try {
            log.info("🔍 [UNIVERSAL CRUD] Scanning package: [{}] for @CrudResource", packageName);
            
            ConfigurationBuilder config = new ConfigurationBuilder()
                    .forPackages(packageName)
                    .addScanners(org.reflections.scanners.Scanners.TypesAnnotated)
                    .addUrls(ClasspathHelper.forPackage(packageName))
                    .addUrls(ClasspathHelper.forJavaClassPath());
            
            Reflections reflections = new Reflections(config);
            
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(CrudResource.class);
            log.info("🔍 [UNIVERSAL CRUD] Found {} annotated classes: {}", annotated.size(), annotated);
            for (Class<?> clazz : annotated) {
                registerResource(clazz);
            }
        } catch (Exception e) {
            log.error("Failed to discover resources in package: " + packageName, e);
        }
    }

    @SuppressWarnings("unchecked")
    public void registerResource(Class<?> entityClazz) {
        if (!BaseEntity.class.isAssignableFrom(entityClazz)) return;
        Class<? extends BaseEntity> entityClass = (Class<? extends BaseEntity>) entityClazz;
        doRegister(entityClass);
    }

    private <T extends BaseEntity> void doRegister(Class<T> entityClass) {
        CrudResource annotation = entityClass.getAnnotation(CrudResource.class);
        String path = annotation.path();
        log.info("🚀 [UNIVERSAL CRUD] Registering entity: [{}] at path [/api/v3/{}]", entityClass.getSimpleName(), path);

        Class<?> dtoClass = annotation.dto();
        // If DTO is Void.class (default), use the Entity class itself for structure inspection
        Class<?> inspectionClass = (dtoClass == Void.class) ? entityClass : dtoClass;

        GenericRepository<T> repository = new GenericRepository<>(entityClass, entityManager);

        // Standard Generic Service
        BaseService<T> service = new BaseService<>(entityClass, entityManager);

        CrudInterceptor<T> interceptor = (CrudInterceptor<T>) interceptors.getOrDefault(entityClass, new CrudInterceptor<T>() {});
        List<ResourceMetadata.FieldInfo> fieldMetadata = inspectFields(inspectionClass);

        ResourceMetadata<T> metadata = ResourceMetadata.<T>builder()
                .entityClass(entityClass)
                .basePath(path)
                .repository(repository)
                .service(service)
                .interceptor(interceptor)
                .fields(fieldMetadata)
                .build();

        resources.put(path, metadata);
    }

    private List<ResourceMetadata.FieldInfo> inspectFields(Class<?> clazz) {
        List<ResourceMetadata.FieldInfo> infos = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(ExcludeFromMeta.class)) {
                    continue;
                }

                Map<String, Object> constraints = new HashMap<>();
                boolean required = field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotBlank.class);
                
                if (field.isAnnotationPresent(Size.class)) {
                    Size size = field.getAnnotation(Size.class);
                    constraints.put("min", size.min());
                    constraints.put("max", size.max());
                }
                if (field.isAnnotationPresent(Positive.class)) {
                    constraints.put("positive", true);
                }

                infos.add(new ResourceMetadata.FieldInfo(
                        field.getName(),
                        field.getType().getSimpleName(),
                        required,
                        constraints
                ));
            }
            current = current.getSuperclass();
        }
        return infos;
    }

    public Map<String, ResourceMetadata<?>> getResources() {
        return Collections.unmodifiableMap(resources);
    }

    public ResourceMetadata<?> getMetadata(String path) {
        return resources.get(path);
    }
}
