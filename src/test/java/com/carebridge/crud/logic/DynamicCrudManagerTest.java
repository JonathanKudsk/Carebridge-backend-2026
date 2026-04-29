package com.carebridge.crud.logic;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.core.CrudInterceptor;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DynamicCrudManagerTest {

    private DynamicCrudManager manager;

    @BeforeEach
    void setUp() {
        manager = new DynamicCrudManager();
    }

    @Test
    @Order(1)
    void initialResourcesIsEmpty() {
        assertTrue(manager.getResources().isEmpty(), "Resources map should be empty before any registration");
    }

    @Test
    @Order(2)
    void getMetadataReturnsNullForUnknownPath() {
        assertNull(manager.getMetadata("nonexistent"), "Metadata for unknown path should be null");
    }

    @Test
    @Order(3)
    void registerInterceptorStoresInterceptor() {
        CrudInterceptor<BaseEntity> interceptor = new CrudInterceptor<>() {};
        // registering should not throw
        assertDoesNotThrow(() -> manager.registerInterceptor(BaseEntity.class, interceptor));
    }

    @Test
    @Order(4)
    void registerResourceIgnoresNonBaseEntityClass() {
        // A class that does not extend BaseEntity must be silently ignored
        assertDoesNotThrow(() -> manager.registerResource(String.class));
        assertTrue(manager.getResources().isEmpty());
    }
}
