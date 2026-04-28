package com.carebridge.crud.api;

import com.carebridge.crud.annotations.DynamicDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.ResourceMetadata;
import com.carebridge.crud.logic.core.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

/**
 * [API LAYER]
 * A universal Spring Boot controller that handles CRUD requests for all registered entities.
 * Uses @DynamicDTO to automatically convert entities to filtered Maps.
 */
@RestController
@RequestMapping("/v3")
public class UniversalCrudController {
    private static final Logger log = LoggerFactory.getLogger(UniversalCrudController.class);
    
    private final DynamicCrudManager crudManager;
    private final MappingService mappingService;

    public UniversalCrudController(DynamicCrudManager crudManager, MappingService mappingService) {
        this.crudManager = crudManager;
        this.mappingService = mappingService;
    }

    @GetMapping("/metadata")
    public Map<String, List<ResourceMetadata.FieldInfo>> getMetadata() {
        log.debug("🔍 [UNIVERSAL CRUD] Fetching global metadata");
        return crudManager.getResources().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getFields()
                ));
    }

    @GetMapping("/{resource}")
    @DynamicDTO
    public ResponseEntity<BaseService.Page<? extends BaseEntity>> getAll(
            @PathVariable String resource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);
        return ResponseEntity.ok(metadata.getService().findAll(page, size));
    }

    @GetMapping("/{resource}/{id}")
    @DynamicDTO
    public ResponseEntity<? extends BaseEntity> getById(
            @PathVariable String resource,
            @PathVariable Long id) {

        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);
        return metadata.getService().findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{resource}")
    @DynamicDTO
    @Transactional
    public ResponseEntity<BaseEntity> create(
            @PathVariable String resource,
            @RequestBody Map<String, Object> body) {

        ResourceMetadata<BaseEntity> metadata = (ResourceMetadata<BaseEntity>) getMetadataOrThrow(resource);
        BaseEntity entity = (BaseEntity) mappingService.toEntity(body, metadata.getEntityClass());
        
        metadata.getInterceptor().beforeCreate(entity);
        BaseEntity saved = metadata.getService().save(entity);
        metadata.getInterceptor().afterCreate(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{resource}/{id}")
    @DynamicDTO
    @Transactional
    public ResponseEntity<BaseEntity> update(
            @PathVariable String resource,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        ResourceMetadata<BaseEntity> metadata = (ResourceMetadata<BaseEntity>) getMetadataOrThrow(resource);
        BaseEntity entity = (BaseEntity) mappingService.toEntity(body, metadata.getEntityClass());
        
        metadata.getInterceptor().beforeUpdate(entity);
        BaseEntity updated = metadata.getService().update(id, entity);
        metadata.getInterceptor().afterUpdate(updated);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{resource}/{id}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable String resource,
            @PathVariable Long id) {
        
        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);

        metadata.getInterceptor().beforeDelete(id);
        metadata.getService().deleteById(id);
        metadata.getInterceptor().afterDelete(id);

        return ResponseEntity.noContent().build();
    }

    private ResourceMetadata<?> getMetadataOrThrow(String resource) {
        ResourceMetadata<?> metadata = crudManager.getMetadata(resource);
        if (metadata == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }
        return metadata;
    }
}
