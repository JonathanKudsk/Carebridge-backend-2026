package com.carebridge.entities;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.Role;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.MappingService;    
import com.carebridge.crud.logic.ResourceMetadata;
import org.junit.jupiter.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;


import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityCoverageTest {

    

    @Test
    @Order(1)
    void testUser() {
        User u = new User();
        u.setName("Name");
        u.setEmail("email");
        u.setPassword("pass");
        u.setRole(Role.USER);
        u.setDisplayEmail("de");
        u.setDisplayName("dn");
        u.setDisplayPhone("dp");
        u.setInternalEmail("ie");
        u.setInternalPhone("ip");
        u.setResidents(new ArrayList<>());

        assertEquals("Name", u.getName());
        assertEquals("email", u.getEmail());
        assertEquals(Role.USER, u.getRole());
        assertEquals("de", u.getDisplayEmail());
        assertEquals("dn", u.getDisplayName());
        assertEquals("dp", u.getDisplayPhone());
        assertEquals("ie", u.getInternalEmail());
        assertEquals("ip", u.getInternalPhone());
        assertNotNull(u.getResidents());
        assertNotNull(u.getPasswordHash());
        
        Resident r = new Resident();
        r.setId(1L);
        u.addResident(r);
        assertTrue(u.getResidents().contains(r));
        u.addResident(r); // Duplicate branch
        u.addResident(null); // Null branch
        
        u.prePersist();
        assertNotNull(u.getCreated_at());
        assertNotNull(u.getUpdated_at());
        u.preUpdate();

        assertTrue(u.verifyPassword("pass"));
        assertFalse(u.verifyPassword("wrong"));
        assertFalse(u.verifyPassword(null)); // Null password verify branch
        
        u.addRole(Role.ADMIN);
        assertEquals(Role.ADMIN, u.getRole());
        u.addRole(null); // Null role branch
        assertEquals(Role.ADMIN, u.getRole());
        
        u.removeRole("ADMIN");
        assertEquals(Role.USER, u.getRole());
        u.removeRole(null); // Null roleName branch
        u.removeRole("WRONG"); // Wrong roleName branch
        
        // Constructor branches
        User u2 = new User("n", "e", "p", null);
        assertEquals(Role.USER, u2.getRole());
        
        assertThrows(IllegalArgumentException.class, () -> u.setPassword(null));
        assertThrows(IllegalArgumentException.class, () -> u.setPassword(""));
        
        // Equals and HashCode
        assertTrue(u.equals(u));
        assertFalse(u.equals(null));
        assertFalse(u.equals(new Object()));
        
        User u3 = new User();
        assertFalse(u.equals(u3)); // One ID null
        u.setId(1L);
        assertFalse(u.equals(u3)); // Other ID null
        u3.setId(2L);
        assertFalse(u.equals(u3)); // Different IDs
        u3.setId(1L);
        assertTrue(u.equals(u3)); // Same IDs
        
        assertEquals(Objects.hashCode(1L), u.hashCode());
    }

    @Test
    @Order(2)
    void testResident() {
        Resident r = new Resident();
        r.setFirstName("F");
        r.setLastName("L");
        r.setCprNr("C");
        r.setJournal(new Journal());
        r.setUsers(new HashSet<>());

        Resident r2 = new Resident("F", "L", "C", new Journal(), null);

        assertEquals("F", r.getFirstName());
        assertEquals("L", r.getLastName());
        assertEquals("C", r.getCprNr());
        assertNotNull(r.getJournal());
        assertNotNull(r.getUsers());

        User u = new User();
        r.addUser(u);
        assertTrue(r.getUsers().contains(u));
        r.removeUser(u);
        assertFalse(r.getUsers().contains(u));
    }

    @Test
    @Order(3)
    void testJournal() {
        Journal j = new Journal();
        Resident r = new Resident();
        j.setResident(r);
        j.setEntries(new ArrayList<>());

        assertEquals(r, j.getResident());
        assertNotNull(j.getEntries());
        
        JournalEntry entry = new JournalEntry();
        j.addEntry(entry);
        assertTrue(j.getEntries().contains(entry));


    }

    @Test
    @Order(4)
    void testJournalEntry() {
        JournalEntry e = new JournalEntry();
        e.setTitle("T");
        e.setContent("C");
        e.setEntryType(EntryType.DAILY);
        e.setRiskAssessment(RiskAssessment.LOW);
        User u = new User();
        e.setAuthor(u);
        Journal j = new Journal();
        e.setJournal(j);
        e.setEditCloseTime(LocalDateTime.now());
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());

        assertEquals("T", e.getTitle());
        assertEquals("C", e.getContent());
        assertEquals(EntryType.DAILY, e.getEntryType());
        assertEquals(RiskAssessment.LOW, e.getRiskAssessment());
        assertEquals(u, e.getAuthor());
        assertEquals(j, e.getJournal());
        assertNotNull(e.getCreatedAt());
        assertNotNull(e.getUpdatedAt());
        assertNotNull(e.getEditCloseTime());

        JournalEntry e2 = new JournalEntry(u, "T2", "C2", RiskAssessment.HIGH, EntryType.INCIDENT);
        assertEquals("T2", e2.getTitle());
        
        // Lifecycle branches
        JournalEntry e3 = new JournalEntry();
        e3.onCreate();
        assertNotNull(e3.getCreatedAt());
        assertNotNull(e3.getEditCloseTime());
        
        e3.setEditCloseTime(null);
        e3.onCreate(); // Branch for null editCloseTime
        assertNotNull(e3.getEditCloseTime());
        
        e3.onUpdate();
        assertNotNull(e3.getUpdatedAt());
    }


    @Test
    @Order(5)
    void ResourceMetadataBuilder() {
        ResourceMetadata.Builder<User> builder = new ResourceMetadata.Builder<>();
        builder.entityClass(User.class);
        builder.basePath("/users");
        builder.service(null);
        builder.interceptor(null);
        builder.fields(new ArrayList<>());
        
        ResourceMetadata<User> metadata = builder.build();
        assertEquals(User.class, metadata.getEntityClass());
        assertEquals("/users", metadata.getBasePath());
        assertNull(metadata.getService());
        assertNull(metadata.getInterceptor());
        assertNotNull(metadata.getFields());
        assertNull(metadata.getRepository());
    }

    @Test
    @Order(6)
    void testFieldInfo() {
        Map<String, Object> constraints = Map.of("maxLength", 255);
        
        // Assuming FieldInfo has an all-args constructor. If it uses setters, adjust accordingly.
        ResourceMetadata.FieldInfo info = new ResourceMetadata.FieldInfo("email", "String", true, constraints);

        assertEquals("email", info.getName());
        assertEquals("String", info.getType());
        assertTrue(info.isRequired());
        assertEquals(constraints, info.getConstraints());
    }
}
