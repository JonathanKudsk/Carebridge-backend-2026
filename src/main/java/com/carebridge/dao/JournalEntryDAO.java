package com.carebridge.dao;

import com.carebridge.models.JournalEntry;
import com.carebridge.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class JournalEntryDAO {


    public void save(JournalEntry journalEntry) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(journalEntry);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) tx.rollback();
        }
    }

    public JournalEntry findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(JournalEntry.class, id);
        }
    }

    public List<JournalEntry> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from JournalEntry", JournalEntry.class).list();
        }
    }

    //Finding all entries by a certain journal ID
    public List<Long> getEntryIdsByJournalId(Long journalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT je.id FROM JournalEntry je WHERE je.journal.id = :journalId",
                            Long.class
                    )
                    .setParameter("journalId", journalId)
                    .list();
        }
    }

    //CRUD operations
    public void update(JournalEntry journalEntry) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(journalEntry);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void create(JournalEntry journalEntry) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(journalEntry);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
