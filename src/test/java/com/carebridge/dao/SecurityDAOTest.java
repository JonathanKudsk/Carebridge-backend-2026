package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.security.SecurityDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ValidationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityDAOTest {

    @Autowired
    private SecurityDAO securityDAO;

    private String email;
    private Long createdId;

    @BeforeEach
    void setUp() {
        email = "sec" + System.nanoTime() + "@test.com";
        User user = securityDAO.createUser("Sec", email, "pass", "DN", "DE@test.com", "DP", "IE@test.com", "IP", Role.USER);
        createdId = user.getId();
    }

    @Test
    @Order(1)
    void testVerifyUserBranches() throws ValidationException {
        // Success
        User verified = securityDAO.getVerifiedUser(email, "pass");
        assertNotNull(verified);
        
        // Wrong password
        assertThrows(ValidationException.class, () -> securityDAO.getVerifiedUser(email, "wrong"));
        
        // No result branch (readByEmail returns null)
        assertThrows(ValidationException.class, () -> securityDAO.getVerifiedUser("nonexistent@test.com", "pass"));
    }

    @Test
    @Order(2)
    void testChangeRoleBranches() {
        User updated = securityDAO.changeRole(createdId, Role.ADMIN);
        assertEquals(Role.ADMIN, updated.getRole());
        
        // user null branch
        assertNull(securityDAO.changeRole(999999L, Role.USER));
    }
}
