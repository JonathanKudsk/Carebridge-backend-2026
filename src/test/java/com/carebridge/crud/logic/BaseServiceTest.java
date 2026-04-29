package com.carebridge.crud.logic;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.entities.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class BaseServiceTest {

    @Autowired
    private EntityManager em;

    private BaseService<User> userService;

    @BeforeEach
    void setUp() {
        userService = new BaseService<>(User.class, em);
    }

    @Test
    @Order(1)
    void testSaveAndFindById() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.addRole(com.carebridge.enums.Role.CAREWORKER);

        User saved = userService.save(user);
        assertNotNull(saved.getId());

        Optional<User> found = userService.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    @Order(2)
    void testUpdate() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test2@test.com");
        user.setPassword("password123");
        user.addRole(com.carebridge.enums.Role.CAREWORKER);

        User saved = userService.save(user);
        
        saved.setName("Updated User");
        User updated = userService.update(saved.getId(), saved);
        
        assertEquals("Updated User", updated.getName());
    }

    @Test
    @Order(3)
    void testDelete() {
        User user = new User();
        user.setName("Test Delete");
        user.setEmail("delete@test.com");
        user.setPassword("password123");
        user.addRole(com.carebridge.enums.Role.CAREWORKER);

        User saved = userService.save(user);
        Long id = saved.getId();

        userService.deleteById(id);

        Optional<User> found = userService.findById(id);
        assertFalse(found.isPresent());
        assertDoesNotThrow(() -> userService.deleteById(999999L));
    }

    @Test
    @Order(4)
    void testFindAll() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@test.com");
        user1.setPassword("pass");
        user1.addRole(com.carebridge.enums.Role.CAREWORKER);
        userService.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@test.com");
        user2.setPassword("pass");
        user2.addRole(com.carebridge.enums.Role.ADMIN);
        userService.save(user2);

        List<User> all = userService.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    @Order(5)
    void testFindAllPaged() {
        User user1 = new User();
        user1.setName("User Paged 1");
        user1.setEmail("paged1@test.com");
        user1.setPassword("pass");
        user1.addRole(com.carebridge.enums.Role.GUARDIAN);
        userService.save(user1);

        User user2 = new User();
        user2.setName("User Paged 2");
        user2.setEmail("paged2@test.com");
        user2.setPassword("pass");
        user2.addRole(com.carebridge.enums.Role.CAREWORKER);
        userService.save(user2);

        BaseService.Page<User> page = userService.findAll(0, 1);
        assertEquals(1, page.getContent().size());
        assertTrue(page.getTotalElements() >= 2);
    }
    
    @Test
    @Order(6)
    void testSaveExistingEntity() {
        User user = new User();
        user.setName("Existing");
        user.setEmail("existing@test.com");
        user.setPassword("pass");
        user.addRole(com.carebridge.enums.Role.CAREWORKER);
        User saved = userService.save(user);
        
        saved.setName("Still Existing");
        User updated = userService.save(saved); // Should call update internally
        
        assertEquals("Still Existing", updated.getName());
    }
    
    @Test
    @Order(7)
    void testUpdateNonExistingEntity() {
        User user = new User();
        user.setName("Ghost");
        user.setEmail("ghost@test.com");
        user.setPassword("pass");
        
        assertThrows(RuntimeException.class, () -> {
            userService.update(999999L, user);
        });
    }
}
