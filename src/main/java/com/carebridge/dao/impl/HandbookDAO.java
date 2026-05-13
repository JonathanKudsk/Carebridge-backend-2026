package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.entities.Handbook;
import com.carebridge.dao.IDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HandbookDAO implements IDAO<Handbook, Long>
{
    private static final Logger logger = LoggerFactory.getLogger(HandbookDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static HandbookDAO instance;


    //Singleton pattern to ensure only one DAO instance exists.
    public static synchronized HandbookDAO getInstance()
    {
        if (instance == null)
        {
            instance = new HandbookDAO();
        }

        return instance;
    }

    @Override
    public Handbook create(Handbook handbook)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(handbook);
            em.getTransaction().commit();

            return handbook;
        }
        catch (Exception e)
        {
            logger.error("Error persisting handbook to db", e);
            throw new RuntimeException(
                    "Error persisting handbook to db.",
                    e
            );
        }
    }


    //Retrieves a handbook including related tabs. Tabs are fetched because they are an essential for the Handbook.
    @Override
    public Handbook read(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Handbook handbook =
                    em.createQuery(
                                    """
                                    SELECT h
                                    FROM Handbook h
                                    LEFT JOIN FETCH h.handbookTabs
                                    WHERE h.id = :id
                                    """,
                                    Handbook.class
                            )
                            .setParameter("id", id)
                            .getSingleResult();

            if (handbook == null)
            {
                throw new EntityNotFoundException(
                        "Handbook not found with ID: " + id
                );
            }

            return handbook;
        }
        catch (EntityNotFoundException e)
        {
            logger.error("Handbook not found", e);

            throw e;
        }
        catch (Exception e)
        {
            logger.error("Error retrieving handbook from db", e);

            throw new RuntimeException(
                    "Error retrieving handbook from db.",
                    e
            );
        }
    }


    //Retrieves all handbooks including related tabs. Mainly useful for system-wide administration.
    @Override
    public List<Handbook> readAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            List<Handbook> handbooks =
                    em.createQuery(
                                    """
                                    SELECT DISTINCT h
                                    FROM Handbook h
                                    LEFT JOIN FETCH h.handbookTabs
                                    """,
                                    Handbook.class
                            )
                            .getResultList();

            if (handbooks.isEmpty())
            {
                throw new EntityNotFoundException(
                        "No handbooks found"
                );
            }

            return handbooks;
        }
        catch (Exception e)
        {
            logger.error("Error retrieving handbooks from db", e);

            throw new RuntimeException(
                    "Error retrieving handbooks from db.",
                    e
            );
        }
    }

    //Updates handbook properties. Currently only updates the title.
    @Override
    public Handbook update(Long id, Handbook updatedHandbook)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();

            Handbook existingHandbook =
                    em.find(Handbook.class, id);

            if (existingHandbook == null)
            {
                throw new EntityNotFoundException(
                        "Handbook not found with ID: " + id
                );
            }

            existingHandbook.setTitle(
                    updatedHandbook.getTitle()
            );

            Handbook mergedHandbook =
                    em.merge(existingHandbook);

            em.getTransaction().commit();

            return mergedHandbook;
        }
        catch (Exception e)
        {
            logger.error("Error updating handbook in db", e);

            throw new RuntimeException(
                    "Error updating handbook in db.",
                    e
            );
        }
    }


    //Handbook deletion is currently not supported.
    @Override
    public void delete(Long id)
    {
        throw new UnsupportedOperationException(
                "Deleting handbook is not supported."
        );
    }
}