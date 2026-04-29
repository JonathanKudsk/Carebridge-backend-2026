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
    void shouldReturnEmptyResourcesMapWhenInitialized() {
        assertTrue(manager.getResources().isEmpty(), "Resources map should be empty before any registration");
    }

    @Test
    @Order(2)
    void shouldReturnNullWhenGettingMetadataForUnknownPath() {
        assertNull(manager.getMetadata("nonexistent"), "Metadata for unknown path should be null");
    }

    @Test
    @Order(3)
    void shouldNotThrowWhenRegisteringInterceptor() {
        CrudInterceptor<BaseEntity> interceptor = new CrudInterceptor<>() {};
        assertDoesNotThrow(() -> manager.registerInterceptor(BaseEntity.class, interceptor));
    }

    @Test
    @Order(4)
    void shouldIgnoreResourceWhenRegisteringNonBaseEntityClass() {
        assertDoesNotThrow(() -> manager.registerResource(String.class));
        assertTrue(manager.getResources().isEmpty());
    }
}
