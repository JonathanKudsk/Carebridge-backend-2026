package com.carebridge;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.carebridge.util.HibernateUtil;

public class DbTest {
    public static void main(String[] args) {
        try {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            Session session = sessionFactory.openSession();

            // Prøv en simpel SQL-forespørgsel
            Object result = session.createNativeQuery("SELECT NOW()").getSingleResult();
            System.out.println("Database connection successful! Time: " + result);

            session.close();
            sessionFactory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
