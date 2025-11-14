// java
package com.carebridge.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import io.github.cdimascio.dotenv.Dotenv;

public class HibernateUtil {
    // lazy-initialized
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(System.getProperty("user.dir"))
                    .ignoreIfMissing()
                    .load();

            String dbUrl = dotenv.get("DATABASE_URL");
            if (dbUrl == null || dbUrl.isBlank()) {
                dbUrl = System.getenv("DATABASE_URL");
            }
            if (dbUrl == null || dbUrl.isBlank()) {
                throw new IllegalStateException("DATABASE_URL is not set in .env or system environment");
            }

            Configuration config = new Configuration();
            config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            config.setProperty("hibernate.connection.url", dbUrl);
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            config.setProperty("hibernate.hbm2ddl.auto", "update");
            config.setProperty("hibernate.show_sql", "true");

            config.addAnnotatedClass(com.carebridge.models.User.class);
            config.addAnnotatedClass(com.carebridge.models.Journal.class);
            config.addAnnotatedClass(com.carebridge.models.JournalEntry.class);

            return config.buildSessionFactory();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to build Hibernate SessionFactory", ex);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
