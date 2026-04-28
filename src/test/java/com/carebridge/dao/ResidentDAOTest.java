package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentDAOTest {

    @Autowired
    private ResidentDAO residentDAO;

    private Long createdId;
    private String cpr;

    @BeforeEach
    void setUp() {
        Resident resident = new Resident();
        resident.setFirstName("John");
        resident.setLastName("Doe");
        cpr = "RES" + System.nanoTime();
        resident.setCprNr(cpr);
        Resident created = residentDAO.create(resident);
        createdId = created.getId();
    }

    @Test
    @Order(1)
    void testCreateReadBranches() {
        assertNotNull(residentDAO.read(createdId));
        // Changed: now returns null instead of throwing
        assertNull(residentDAO.read(999999L));
        assertThrows(ApiRuntimeException.class, () -> residentDAO.create(null));
    }

    @Test
    @Order(2)
    void testReadByCprBranches() {
        assertNotNull(residentDAO.readByCpr(cpr));
        assertNull(residentDAO.readByCpr("NONEXISTENT"));
    }

    @Test
    @Order(3)
    void testUpdateBranches() {
        Resident patch = new Resident();
        patch.setFirstName("Bobby");
        patch.setLastName("Jones");
        patch.setCprNr("NEW" + System.nanoTime());
        Resident updated = residentDAO.update(createdId, patch);
        assertEquals("Bobby", updated.getFirstName());
        assertEquals("Jones", updated.getLastName());

        // Partial update branches
        Resident patch2 = new Resident();
        patch2.setFirstName(null);
        patch2.setLastName(null);
        patch2.setCprNr(null);
        Resident updated2 = residentDAO.update(createdId, patch2);
        assertEquals("Bobby", updated2.getFirstName());
        
        assertNull(residentDAO.update(999999L, new Resident()));
    }

    @Test
    @Order(4)
    void testDeleteBranches() {
        // delete non-existent (null check branch)
        residentDAO.delete(999999L);
        // delete existent
        residentDAO.delete(createdId);
        assertNull(residentDAO.read(createdId));
    }
    
    @Test
    @Order(5)
    void testReadAll() {
        assertFalse(residentDAO.readAll().isEmpty());
    }
}
