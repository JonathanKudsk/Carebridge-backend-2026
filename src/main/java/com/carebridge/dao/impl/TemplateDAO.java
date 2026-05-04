package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Template;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TemplateDAO implements IDAO<Template,Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventTypeDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static TemplateDAO instance;

    private TemplateDAO() {
    }

    public static synchronized TemplateDAO getInstance() {
        if (instance == null) instance = new TemplateDAO();
        return instance;
    }

    @Override
    public Template read(Long id) {
        try (var em = em()) {
            return em.createQuery("SELECT t FROM Template t LEFT JOIN FETCH t.fields where t.id = :id and t.isUsable = TRUE", Template.class).setParameter("id",id)
                    .getSingleResult();
        } catch (NoResultException e) { //if nothing exists in DB return nothing
            return null;
        }
        catch (Exception e) {
            logger.error("Error finding Template", e);
            throw new ApiRuntimeException(500, "Error finding Template: " + e.getMessage());
        }
    }

    @Override
    public List<Template> readAll() {
        try (var em = em()) {
            return em.createQuery("SELECT t FROM Template t where t.isUsable = TRUE ORDER BY t.id  ", Template.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Templates", e);
            throw new ApiRuntimeException(500, "Error reading all Templates: " + e.getMessage());
        }
    }

    @Override
    public Template create(Template template) {
        return null; //todo: missing implementation
    }

    @Override
    public Template update(Long id, Template template) {
        return null; //todo: missing implementation
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();

                int amountUpdated = em.createQuery("UPDATE Template t SET t.isUsable = FALSE WHERE t.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();

                if (amountUpdated < 1) {
                    transaction.rollback();
                    throw new ApiRuntimeException(404, "no Template was found");
                }
                if (amountUpdated > 1) {
                    transaction.rollback();
                    throw new ApiRuntimeException(500, "more rows were updated than possible");
                }

                transaction.commit();
            } catch (ApiRuntimeException e) {
                if (transaction.isActive()) transaction.rollback();
                throw e;
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                logger.error("Error deleting Template", e);
                throw new ApiRuntimeException(500, "Error Deleting Template: " + e.getMessage());
            }
        }
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }
}
