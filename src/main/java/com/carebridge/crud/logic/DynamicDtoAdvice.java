package com.carebridge.crud.logic;

import com.carebridge.crud.annotations.DynamicDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.core.BaseService;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🪄 THE MAGIC DTO ADVICE
 * Automatically applies MappingService to any method annotated with @DynamicDTO.
 */
@ControllerAdvice
public class DynamicDtoAdvice implements ResponseBodyAdvice<Object> {

    private final MappingService mappingService;

    public DynamicDtoAdvice(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(DynamicDTO.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body == null) return null;

        // Handle single Entity
        if (body instanceof BaseEntity entity) {
            return mappingService.toMap(entity);
        }

        // Handle List of Entities
        if (body instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> (item instanceof BaseEntity be) ? mappingService.toMap(be) : item)
                    .collect(Collectors.toList());
        }
        
        // Handle BaseService.Page
        if (body instanceof BaseService.Page<?> page) {
            List<Map<String, Object>> mappedContent = page.getContent().stream()
                    .map(item -> (item instanceof BaseEntity be) ? mappingService.toMap(be) : (Map<String, Object>)item)
                    .collect(Collectors.toList());
            return Map.of(
                "content", mappedContent,
                "totalElements", page.getTotalElements()
            );
        }

        return body;
    }
}
