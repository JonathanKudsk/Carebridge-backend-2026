package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Location;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubstituteDAOTest {

    private EntityManagerFactory emf;
    private UserDAO userDAO;

    private Long centralLocationId;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        cleanup();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Location central = new Location("Central Care", "Big Street 1");
            Location north = new Location("North Care", "Big Street 2");

            User anna = createSubstitute(
                    "Anna Vikar",
                    "anna.sub@example.com",
                    "anna@example.com",
                    "11111111",
                    "anna.intern@example.com",
                    "22222222"
            );
            User bo = createSubstitute(
                    "Bo Vikar",
                    "bo.sub@example.com",
                    "bo@example.com",
                    "33333333",
                    "bo.intern@example.com",
                    "44444444"
            );

            central.addUser(anna);
            north.addUser(bo);

            em.persist(anna);
            em.persist(bo);
            em.persist(central);
            em.persist(north);

            em.getTransaction().commit();
            centralLocationId = central.getId();
        }
    }

    @AfterEach
    public void cleanup() {
        if (emf == null) return;

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            List<Location> locations = em.createQuery(
                            "SELECT l FROM Location l WHERE l.locationName IN :names",
                            Location.class)
                    .setParameter("names", List.of("Central Care", "North Care"))
                    .getResultList();
            locations.forEach(location -> location.getUsers().clear());

            List<User> substitutes = em.createQuery(
                            "SELECT s FROM User s WHERE s.email IN :emails",
                            User.class)
                    .setParameter("emails", List.of("anna.substitute@example.com", "bo.substitute@example.com"))
                    .getResultList();
            substitutes.forEach(substitute -> substitute.getLocations().clear());

            locations.forEach(em::remove);
            substitutes.forEach(em::remove);

            em.getTransaction().commit();
        }
    }

    @Test
    public void readAllSubstitutesReturnsContactInfoAndLocations() {
        List<User> substitutes = userDAO.readAllSubstitutes(null, null);

        User anna = substitutes.stream()
                .filter(substitute -> substitute.getEmail().equals("anna.substitute@example.com"))
                .findFirst()
                .orElseThrow();

        assertEquals("Anna Substitute", anna.getName());
        assertEquals("anna.display@example.com", anna.getDisplayEmail());
        assertEquals("11111111", anna.getDisplayPhone());
        assertEquals("anna.internal@example.com", anna.getInternalEmail());
        assertEquals("22222222", anna.getInternalPhone());
        assertEquals("Central Care", anna.getLocations().iterator().next().getLocationName());
    }

    @Test
    public void readAllSubstitutesCanFilterByLocationId() {
        List<User> substitutes = userDAO.readAllSubstitutes(centralLocationId, null);

        assertEquals(1, substitutes.size());
        assertEquals("anna.substitute@example.com", substitutes.get(0).getEmail());
    }

    @Test
    public void readAllSubstitutesCanFilterByLocationName() {
        List<User> substitutes = userDAO.readAllSubstitutes(null, "north");

        assertEquals(1, substitutes.size());
        assertEquals("bo.substitute@example.com", substitutes.get(0).getEmail());
    }

    @Test
    public void readSubstituteLocationsReturnsLocationsUsedBySubstitutes() {
        List<Location> locations = userDAO.readSubstituteLocations();

        assertTrue(locations.stream().anyMatch(location -> location.getLocationName().equals("Central Care")));
        assertTrue(locations.stream().anyMatch(location -> location.getLocationName().equals("North Care")));
    }

    private User createSubstitute(
            String name,
            String email,
            String displayEmail,
            String displayPhone,
            String internalEmail,
            String internalPhone
    ) {
        User substitute = new User();
        substitute.setName(name);
        substitute.setEmail(email);
        substitute.setPassword("password123");
        substitute.setRole(Role.SUBSTITUTE);
        substitute.setDisplayEmail(displayEmail);
        substitute.setDisplayPhone(displayPhone);
        substitute.setInternalEmail(internalEmail);
        substitute.setInternalPhone(internalPhone);
        return substitute;
    }
}
