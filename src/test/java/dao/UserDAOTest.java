package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOTest {

    private UserDAO userDAO;
    private User testUser;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        EntityManagerFactory emfTest = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        // Opret en frisk bruger for hver test
        testUser = new User();
        testUser.setName("Shared Test User");
        testUser.setEmail("shareduser@example.com");
        testUser.setRole(Role.USER);
        testUser.setPassword("test123");
        userDAO.create(testUser);
    }

    @AfterEach
    public void cleanup() {
        if (testUser != null) {
            try {
                userDAO.delete(testUser.getId());
            } catch (Exception ignored) {}
        }
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setName("Create User");
        user.setEmail("createuser@example.com");
        user.setRole(Role.USER);
        user.setPassword("create123");
        User created = userDAO.create(user);
        assertNotNull(created.getId());

        userDAO.delete(created.getId());
    }

    @Test
    public void testReadUser() {
        User read = userDAO.read(testUser.getId());
        assertEquals("Shared Test User", read.getName());
    }

    @Test
    public void testUpdateUser() {
        testUser.setName("Updated Name");
        User updated = userDAO.update(testUser.getId(), testUser);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    public void testReadByEmail() {
        User byEmail = userDAO.readByEmail(testUser.getEmail());
        assertNotNull(byEmail);
        assertEquals(testUser.getId(), byEmail.getId());
    }

    @Test
    public void testReadAllUsers() {
        List<User> users = userDAO.readAll();
        assertTrue(users.size() >= 1);
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setName("Delete User");
        user.setEmail("deleteuser@example.com");
        user.setRole(Role.USER);
        user.setPassword("delete123");
        User created = userDAO.create(user);

        userDAO.delete(created.getId());
        assertNull(userDAO.read(created.getId()));
    }
}
