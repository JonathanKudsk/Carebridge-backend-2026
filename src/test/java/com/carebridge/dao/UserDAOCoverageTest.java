package com.carebridge.dao;

import com.carebridge.dao.impl.UserDAO;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOCoverageTest {

    private UserDAO userDAO;
    private ThrowingEntityManager em;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        em = new ThrowingEntityManager();
        ReflectionTestUtils.setField(userDAO, "em", em);
    }

    @Test
    @Order(1)
    void testReadException() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.read(1L));
    }

    @Test
    @Order(2)
    void testReadByEmailException() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail("test@test.com"));
    }

    @Test
    @Order(3)
    void testReadAllException() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.readAll());
    }
}
