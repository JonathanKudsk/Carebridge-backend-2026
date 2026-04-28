package com.carebridge.config;

import com.carebridge.CareBridgeApplication;
import com.carebridge.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Populator populator;

    @Autowired
    private EntityManagerFactory emf;

    @PersistenceContext
    private EntityManager em;

    @Test
    @Order(1)
    void testApplicationConfig() {
        assertNotNull(objectMapper);
    }

    @Test
    @Order(2)
    @Transactional
    void testPopulatorFirstRun() {
        // Clear everything to trigger creation
        em.createQuery("DELETE FROM User WHERE email = 'admin@carebridge.io'").executeUpdate();
        
        populator.populate();

        // Verify some were created
        User admin = em.createQuery("SELECT u FROM User u WHERE u.email = 'admin@carebridge.io'", User.class).getSingleResult();
        assertNotNull(admin);
        
    }

    @Test
    @Order(3)
    @Transactional
    void testPopulatorSecondRun() {
        // Ensure some exist
        populator.populate(); 
        
        // Second run should hit the "already exists" branches
        populator.populate();
    }

    @Test
    @Order(4)
    void testPopulatorRunner() throws Exception {
        PopulatorRunner runner = new PopulatorRunner(populator);
        runner.run();
    }

    @Test
    @Order(5)
    void testLegacyPopulateSuccess() {
        Populator.populate(emf);
    }

    @Test
    @Order(6)
    void testLegacyPopulateFailure() {
        // Use Proxy to avoid Mockito/ByteBuddy issues on Java 26
        EntityManagerFactory proxyEmf = (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("createEntityManager")) {
                        return Proxy.newProxyInstance(
                                EntityManager.class.getClassLoader(),
                                new Class<?>[]{EntityManager.class},
                                (emProxy, emMethod, emArgs) -> {
                                    if (emMethod.getName().equals("getTransaction")) {
                                        return Proxy.newProxyInstance(
                                                EntityTransaction.class.getClassLoader(),
                                                new Class<?>[]{EntityTransaction.class},
                                                (txProxy, txMethod, txArgs) -> {
                                                    if (txMethod.getName().equals("commit")) {
                                                        throw new RuntimeException("Forced failure");
                                                    }
                                                    return null;
                                                }
                                        );
                                    }
                                    return null;
                                }
                        );
                    }
                    return null;
                }
        );
        
        assertDoesNotThrow(() -> Populator.populate(proxyEmf));
    }
}
