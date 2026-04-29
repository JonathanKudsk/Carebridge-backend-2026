package com.carebridge.crud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🎨 DYNAMIC DTO ANNOTATION
 * Marks a controller method to automatically convert the returned Entity or Collection
 * into a Dynamic DTO (Map) using the MappingService.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicDTO {
}
