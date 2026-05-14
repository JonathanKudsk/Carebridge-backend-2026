package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.StaffJournalDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.StaffJournal;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StaffJournalDAOTest {

    private StaffJournalDAO journalDAO;
    private UserDAO userDAO;

    @BeforeAll
    void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        journalDAO = StaffJournalDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    private User createUser(String email, Role role) {
        User u = new User();
        u.setName("DAO Test User");
        u.setEmail(email);
        u.setRole(role);
        u.setPassword("pass123");
        return userDAO.create(u);
    }

    private StaffJournal createJournalFor(User user) {
        StaffJournal journal = new StaffJournal();
        journal.setUser(user);
        return journalDAO.create(journal);
    }

    @Test
    void create_setsGeneratedId() {
        User user = createUser("dao_create_" + System.currentTimeMillis() + "@test.com", Role.CAREWORKER);
        StaffJournal created = createJournalFor(user);
        assertNotNull(created.getId());
    }

    @Test
    void readByUserId_returnsCorrectJournal() {
        User user = createUser("dao_readbyuser_" + System.currentTimeMillis() + "@test.com", Role.ADMIN);
        StaffJournal created = createJournalFor(user);
        StaffJournal found = journalDAO.readByUserId(user.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(user.getId(), found.getUser().getId());
    }

    @Test
    void readById_returnsCorrectJournal() {
        User user = createUser("dao_readbyid_" + System.currentTimeMillis() + "@test.com", Role.CAREWORKER);
        StaffJournal created = createJournalFor(user);
        StaffJournal found = journalDAO.read(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void guardianUser_noJournalCreated_readByUserIdReturnsNull() {
        User guardian = createUser("dao_guardian_" + System.currentTimeMillis() + "@test.com", Role.GUARDIAN);
        assertNull(journalDAO.readByUserId(guardian.getId()));
    }

    @Test
    void regularUser_noJournalCreated_readByUserIdReturnsNull() {
        User user = createUser("dao_user_" + System.currentTimeMillis() + "@test.com", Role.USER);
        assertNull(journalDAO.readByUserId(user.getId()));
    }
}
