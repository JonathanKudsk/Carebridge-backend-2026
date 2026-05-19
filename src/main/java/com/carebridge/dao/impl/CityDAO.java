package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.City;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CityDAO implements IDAO<City,Long> {

    private static final Logger logger = LoggerFactory.getLogger(CityDAO.class);
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static CityDAO instance;

    private CityDAO() {
    }

    public static synchronized CityDAO getInstance() {
        if (instance == null) instance = new CityDAO();
        return instance;
    }

    @Override
    public City read(Long id) {
        try (var em = em()) {
            return em.createQuery("SELECT C FROM City C where C.id = :id", City.class).setParameter("id",id)
                    .getSingleResult();
        } catch (NoResultException e) { //if nothing exists in DB return nothing
            return null;
        }
        catch (Exception e) {
            logger.error("Error finding City", e);
            throw new ApiRuntimeException(500, "Error finding City: " + e.getMessage());
        }
    }

    public City read(int zip, String cityName) {
        try (var em = em()) {
            return em.createQuery("SELECT C FROM City C where C.cityname = :CityName and C.postalcode = :Zip", City.class)
                    .setParameter("CityName",cityName)
                    .setParameter("Zip",zip)
                    .getSingleResult();
        } catch (NoResultException e) { //if nothing exists in DB return nothing
            return null;
        }
        catch (Exception e) {
            logger.error("Error finding City", e);
            throw new ApiRuntimeException(500, "Error finding City: " + e.getMessage());
        }
    }

    @Override
    public List<City> readAll() {
        try (var em = em()) {
            return em.createQuery("SELECT c FROM City c ORDER BY c.id", City.class)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Error reading all Cities", e);
            throw new ApiRuntimeException(500, "Error reading all Cities: " + e.getMessage());
        }
    }

    @Override
    public City create(City city) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(city);
            em.getTransaction().commit();
            return city;
        } catch (Exception e) {
            logger.error("Error creating City", e);
            throw new ApiRuntimeException(500, "Error creating City: " + e.getMessage());
        }
    }

    @Override
    public City update(Long id, City city) {
        try (var em = em()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                city.setId(id);
                transaction.begin();
                em.merge(city);
                transaction.commit();
                return city;
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                logger.error("Error deleting City", e);
                throw new ApiRuntimeException(500, "Error Deleting City: " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(Long id) {
        try (var em = em()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                City object = em.find(City.class,id);
                em.remove(object);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) transaction.rollback();
                logger.error("Error deleting City", e);
                throw new ApiRuntimeException(500, "Error Deleting City: " + e.getMessage());
            }
        }
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }
}