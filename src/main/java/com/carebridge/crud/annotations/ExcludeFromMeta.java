package com.carebridge.crud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🌐 METADATA ANNOTATION
 * Any field marked with this annotation will be hidden from the /api/v2/metadata discovery endpoint.
 * Useful for hiding internal-only database fields from frontend forms.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeFromMeta {
}
