package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Template;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TemplateDAO implements IDAO<Template,Long> {

    private static final Logger logger = LoggerFactory.getLogger(TemplateDAO.class);
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
            return em.createQuery("SELECT t FROM Template t JOIN FETCH t.fields where t.id = :id", Template.class).setParameter("id",id)
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
            return em.createQuery("SELECT t FROM Template t ORDER BY t.id", Template.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Templates", e);
            throw new ApiRuntimeException(500, "Error reading all Templates: " + e.getMessage());
        }
    }

    @Override
    public Template create(Template template) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(template);
            em.getTransaction().commit();
            return template;
        } catch (Exception e) {
            logger.error("Error persisting Template to db", e);
            throw new RuntimeException("Error persisting Template to db. ", e);
        }
    }

    @Override
    public Template update(Long id, Template template) {
        return null; //todo: missing implementation
    }

    @Override
    public void delete(Long id) {
        //todo: missing implementation
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }
}
