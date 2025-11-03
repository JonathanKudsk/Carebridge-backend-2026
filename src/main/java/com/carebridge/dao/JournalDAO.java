package com.carebridge.dao;

import com.carebridge.models.Journal;
import com.carebridge.models.JournalEntry;
import com.carebridge.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class JournalDAO
{

    public void save(Journal journal) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(journal);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Journal findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Journal.class, id);
        }
    }

    public List<Journal> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Journal", Journal.class).list();
        }
    }

    public void addEntryToJournal(Long journalId, Long journalEntryId) {
        Transaction tx  = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Journal journal = session.get(Journal.class, journalId);
            JournalEntry journalEntry = session.get(JournalEntry.class, journalEntryId);
            journal.addEntry(journalEntry);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }

    }
}
