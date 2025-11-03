package com.carebridge.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import io.github.cdimascio.dotenv.Dotenv;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DATABASE_URL");

            Configuration config = new Configuration();
            config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            config.setProperty("hibernate.connection.url", dbUrl.replace("postgres://", "jdbc:postgresql://"));
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            config.setProperty("hibernate.hbm2ddl.auto", "update");
            config.setProperty("hibernate.show_sql", "true");

            config.addAnnotatedClass(com.carebridge.models.User.class);
            config.addAnnotatedClass(com.carebridge.models.Resident.class);
            config.addAnnotatedClass(com.carebridge.models.JournalEntry.class);

            return config.buildSessionFactory();
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
