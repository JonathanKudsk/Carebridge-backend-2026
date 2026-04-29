package com.carebridge.crud.logic;

import com.carebridge.crud.logic.core.CrudInterceptor;
import com.carebridge.entities.User;
import org.junit.jupiter.api.*;
import com.carebridge.crud.data.core.BaseEntity;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrudInterceptorTest {

    @Test
    @Order(1)
    void testDefaultMethods() {
        CrudInterceptor<User> interceptor = new CrudInterceptor<User>() {};
        User user = new User();
        user.setName("hello");
        user.setEmail("test@ee.dk");
        user.setPassword("pass");
        user.addRole(com.carebridge.enums.Role.CAREWORKER);
        user.setId(1L);

        assertDoesNotThrow(() -> {
            interceptor.beforeCreate(user);
            interceptor.afterCreate(user);
            interceptor.beforeUpdate(user);
            interceptor.afterUpdate(user);
            interceptor.beforeDelete(1L);
            interceptor.afterDelete(1L);
        });

    
    }
}