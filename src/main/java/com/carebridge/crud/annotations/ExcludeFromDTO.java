package com.carebridge.crud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🔒 SECURITY ANNOTATION
 * Any field marked with this annotation will be excluded from the dynamic DTO mapping
 * and will NOT be present in API JSON responses.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeFromDTO {
}
