package com.carebridge.crud.logic;

import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MappingServiceTest {

    private MappingService mappingService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mappingService = new MappingService(mapper);
    }

    @Test
    @Order(1)
    void testToMap_BasicEntity() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        Map<String, Object> map = mappingService.toMap(user);

        assertNotNull(map);
        assertEquals(1L, map.get("id"));
        assertEquals("Test User", map.get("name"));
        assertEquals("test@example.com", map.get("email"));
        assertEquals(Role.USER, map.get("role"));
    }

    @Test
    @Order(2)
    void testToEntity_BasicMap() {
        Map<String, Object> data = Map.of(
            "name", "New User",
            "email", "new@example.com",
            "role", "ADMIN"
        );

        User user = mappingService.toEntity(data, User.class);

        assertNotNull(user);
        assertEquals("New User", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
    }

   
}
