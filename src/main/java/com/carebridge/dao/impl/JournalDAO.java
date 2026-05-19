package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.JournalEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JournalDAO implements IDAO<Journal, Long>
{

    private static final Logger logger = LoggerFactory.getLogger(JournalDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static JournalDAO instance;

    public static synchronized JournalDAO getInstance() {
        if (instance == null) instance = new JournalDAO();
        return instance;
    }

    public Journal create(Journal journal)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(journal);
            em.getTransaction().commit();
            return journal;
        }
        catch (Exception e)
        {
            logger.error("Error persisting object to db", e);
            throw new RuntimeException("Error persisting object to db. ", e);
        }
    }

    public Journal read(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Journal journalEntity = em.find(Journal.class, id);
            if (journalEntity == null)
            {
                throw new RuntimeException("Journal not found with ID: " + id);
            }
            return journalEntity;
        }
        catch (Exception e)
        {
            logger.error("Error retrieving object from db", e);
            throw new RuntimeException("Error retrieving object from db. ", e);
        }
    }

    public List<Journal> readAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            List<Journal> journals = em.createQuery("SELECT j FROM Journal j", Journal.class).getResultList();
            if(journals.isEmpty())
            {
                throw new EntityNotFoundException("No journals found");
            }
            return journals;
        }
    }

    @Override
    public Journal update(Long id, Journal journal)
    {
        return null;
    }

    @Override
    public void delete(Long id)
    {

    }

    public void addEntryToJournal(Journal journal, JournalEntry journalEntry)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            journal.addEntry(journalEntry);
            em.merge(journal);
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            logger.error("Error updating journal with new entry", e);
            throw new RuntimeException("Error updating journal with new entry. ", e);
        }
    }

    // Checks whether a guardian is linked to the resident that owns the requested journal.
    public boolean guardianHasAccessToJournal(String guardianEmail, Long journalId) {
        if (guardianEmail == null || journalId == null) return false;

        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(j) FROM User u " +
                                    "JOIN u.residents r " +
                                    "JOIN r.journal j " +
                                    "WHERE u.email = :email AND j.id = :journalId",
                            Long.class)
                    .setParameter("email", guardianEmail)
                    .setParameter("journalId", journalId)
                    .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking guardian journal access", e);
            throw new RuntimeException("Error checking journal access", e);
        }
    }

}
