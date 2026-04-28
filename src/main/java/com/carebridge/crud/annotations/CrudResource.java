package com.carebridge.crud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity as a Generic CRUD resource.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudResource {
    /**
     * The base path for the API (e.g., "users" -> /api/v3/users).
     */
    String path();

    /**
     * (Optional) The DTO class (Record or Class). 
     * If not provided, the system will use the Entity directly and respect exclusion annotations.
     */
    Class<?> dto() default Void.class;

    /**
     * (Optional) List of roles required to access this resource.
     */
    String[] roles() default {};
}
