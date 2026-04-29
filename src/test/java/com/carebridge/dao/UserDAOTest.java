package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
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
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    private Long createdId;
    private String email;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("DAO Test User");
        email = "daotest" + System.nanoTime() + "@example.com";
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setPassword("password123");
        User created = userDAO.create(user);
        createdId = created.getId();
    }

    @Test
    @Order(1)
    void testUpdateBranchesExhaustive() {
        // Case 1: All null/blank (false branches)
        User p1 = new User();
        p1.setName(" ");
        p1.setEmail(null);
        p1.setRole(null);
        User u1 = userDAO.update(createdId, p1);
        assertEquals("DAO Test User", u1.getName());
        assertEquals(email, u1.getEmail());
        assertEquals(Role.USER, u1.getRole());

        // Case 2: Email blank, Role valid
        User p2 = new User();
        p2.setName(null);
        p2.setEmail("");
        p2.setRole(Role.ADMIN);
        User u2 = userDAO.update(createdId, p2);
        assertEquals(email, u2.getEmail());
        assertEquals(Role.ADMIN, u2.getRole());

        // Case 3: Name valid, Email valid, Role null
        User p3 = new User();
        p3.setName("NewN");
        p3.setEmail("newE@t.com");
        p3.setRole(null);
        User u3 = userDAO.update(createdId, p3);
        assertEquals("NewN", u3.getName());
        assertEquals("newE@t.com", u3.getEmail());
    }

    @Test
    @Order(2)
    void testCreateBranchesExhaustive() {
        // 1. Role null branch
        User u1 = new User("N1", "e1" + System.nanoTime() + "@t.com", "p", null);
        User c1 = userDAO.create(u1);
        assertEquals(Role.USER, c1.getRole());
        
        // 2. Role non-null branch
        User u2 = new User("N2", "e2" + System.nanoTime() + "@t.com", "p", Role.ADMIN);
        User c2 = userDAO.create(u2);
        assertEquals(Role.ADMIN, c2.getRole());

        // 3. Email null branch
        User u3 = new User();
        u3.setName("N3");
        u3.setEmail(null);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u3));

        // 4. Email blank branch (to hit second part of OR)
        u3.setEmail("  ");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u3));

        // 5. Name null branch
        User u4 = new User();
        u4.setEmail("e4" + System.nanoTime() + "@t.com");
        u4.setName(null);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u4));

        // 6. Name blank branch (to hit second part of OR)
        u4.setName("");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u4));

    }

    @Test
    @Order(3)
    void testReadAndReadByEmailBranches() {
        assertNotNull(userDAO.read(createdId));
        assertNull(userDAO.read(999999L));
        
        assertNotNull(userDAO.readByEmail(email));
        assertNull(userDAO.readByEmail("none@test.com"));
        
        // Email null branch in readByEmail
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(null));
        // Email blank branch in readByEmail
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail("  "));
    }

    @Test
    @Order(4)
    void testErrors() {        
        // Duplicate email
        User dup = new User("D", email, "p", Role.USER);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(dup));
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(null));
        assertThrows(ApiRuntimeException.class, () -> userDAO.update(999999L, new User()));
        assertThrows(ApiRuntimeException.class, () -> userDAO.delete(999999L));
    }

    @Test
    @Order(5)
    void testReadAll() {
        assertFalse(userDAO.readAll().isEmpty());
    }

    @Test
    @Order(6)
    void testDelete() {
        userDAO.delete(createdId);
    }

}
