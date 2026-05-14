package com.carebridge.dao.impl;

import jakarta.persistence.criteria.*;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.entities.JournalEntryAnswer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Template;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JournalEntryDAO implements IDAO<JournalEntry, Long> {
    private static final Logger logger = LoggerFactory.getLogger(JournalEntryDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static JournalEntryDAO instance;

    public static synchronized JournalEntryDAO getInstance() {
        if (instance == null)
            instance = new JournalEntryDAO();
        return instance;
    }

    public JournalEntry create(JournalEntry journalEntry) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(journalEntry);
            em.getTransaction().commit();
            return journalEntry;
        } catch (Exception e) {
            logger.error("Error persisting JournalEntry to db", e);
            throw new RuntimeException("Error persisting JournalEntry to db. ", e);
        }
    }

    public JournalEntry read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {

            return em
                    .createQuery("SELECT je FROM JournalEntry je JOIN FETCH je.journalEntryAnswers where je.id = :id",
                            JournalEntry.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) { // if nothing exists in DB return nothing
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving JournalEntry from db", e);
            throw new RuntimeException("Error retrieving JournalEntry from db. ", e);
        }
    }

    public List<JournalEntry> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            List<JournalEntry> entries = em.createQuery("SELECT je FROM JournalEntry je", JournalEntry.class)
                    .getResultList();
            if (entries.isEmpty()) {
                throw new EntityNotFoundException("No journal entries found");
            }
            return entries;
        }
    }

    public List<Long> getEntryIdsByJournalId(Long journalId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT je.id FROM JournalEntry je WHERE je.journal.id = :journalId",
                    Long.class)
                    .setParameter("journalId", journalId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error querying entry IDs by journalId", e);
            throw new RuntimeException("Error querying entry IDs by journalId. ", e);
        }
    }

    public JournalEntry update(Long Id, JournalEntry journalEntry) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            JournalEntry existingEntry = em.find(JournalEntry.class, Id);
            if (existingEntry == null) {
                throw new RuntimeException("JournalEntry not found with ID: " + Id);
            }
            existingEntry.setUpdatedAt(journalEntry.getUpdatedAt());
            em.getTransaction().commit();
            return existingEntry;
        } catch (Exception e) {
            logger.error("Error updating JournalEntry in db", e);
            throw new RuntimeException("Error updating JournalEntry in db. ", e);
        }
    }

    @Override
    public void delete(Long id) {

    }

    public List<JournalEntry> getEntriesByJournalId(Long journalId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT je FROM JournalEntry je WHERE je.journal.id = :journalId",
                    JournalEntry.class)
                    .setParameter("journalId", journalId)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error querying entries by journalId", e);
            throw new RuntimeException("Error querying entries by journalId. ", e);
        }
    }

    public Object[] search(LocalDateTime dateFrom, LocalDateTime dateTo, Long employeeId,
            RiskAssessment riskLevel, String keyword, int page, int pageSize) {
        try (EntityManager em = emf.createEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // 1. Byg count-query (Total tæller til din paginering)
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<JournalEntry> countRoot = countQuery.from(JournalEntry.class);
            List<Predicate> countPredicates = buildSearchPredicates(cb, countRoot, dateFrom, dateTo, employeeId,
                    riskLevel, keyword);

            // Vi SKAL bruge countDistinct, fordi et JOIN længere nede kan skabe
            // midlertidige dubletter
            countQuery.select(cb.countDistinct(countRoot)).where(countPredicates.toArray(new Predicate[0]));
            Long total = em.createQuery(countQuery).getSingleResult();

            // 2. Byg selve data-queryen
            CriteriaQuery<JournalEntry> query = cb.createQuery(JournalEntry.class);
            Root<JournalEntry> root = query.from(JournalEntry.class);
            List<Predicate> predicates = buildSearchPredicates(cb, root, dateFrom, dateTo, employeeId, riskLevel,
                    keyword);

            // distinct(true) sikrer at beboeren ikke får vist samme journal 5 gange, hvis
            // keywordet står i 5 af svarene
            query.select(root).where(predicates.toArray(new Predicate[0])).distinct(true);
            query.orderBy(cb.desc(root.get("createdAt")));

            // 3. Udfør med paginering (Limit og Offset)
            List<JournalEntry> results = em.createQuery(query)
                    .setFirstResult((page - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();

            return new Object[] { results, total };
        } catch (Exception e) {
            logger.error("Fejl under database-søgning", e);
            throw new RuntimeException("Database error under search", e);
        }
    }

    private List<Predicate> buildSearchPredicates(CriteriaBuilder cb, Root<JournalEntry> root,
            LocalDateTime dateFrom, LocalDateTime dateTo,
            Long employeeId, RiskAssessment riskLevel, String keyword) {
        List<Predicate> predicates = new ArrayList<>();

        // Bygger AND-logik for alle de valgfrie parametre
        if (dateFrom != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
        if (dateTo != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
        if (employeeId != null)
            predicates.add(cb.equal(root.get("author").get("id"), employeeId));
        if (riskLevel != null)
            predicates.add(cb.equal(root.get("riskAssessment"), riskLevel));

        // Fuld-tekst søgning
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);

            // Her kobler vi over i JournalEntryAnswer for at lede i det eksakte felt:
            // 'answer'
            Join<JournalEntry, JournalEntryAnswer> answersJoin = root.join("journalEntryAnswers", JoinType.LEFT);
            Predicate answerMatch = cb.like(cb.lower(answersJoin.get("answer")), pattern);

            // Or-logik: Matcher enten titel eller mindst ét svar
            predicates.add(cb.or(titleMatch, answerMatch));
        }
        return predicates;
    }
}