package com.carebridge.dao.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.IDAO;
import com.carebridge.entities.Resident;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ResidentDAO implements IDAO<Resident, Long> {

    private static ResidentDAO instance;
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    private ResidentDAO() {
        // Tom constructor - emf er allerede initialiseret som static felt
    }

    public static ResidentDAO getInstance() {
        if (instance == null) {
            instance = new ResidentDAO();
        }
        return instance;
    }

    @Override
    public Resident read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Resident.class, id);
        }
    }

    @Override
    public List<Resident> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Resident> query = em.createQuery("SELECT r FROM Resident r", Resident.class);
            return query.getResultList();
        }
    }

    @Override
    public Resident create(Resident resident) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(resident);
            em.getTransaction().commit();
            return resident;
        }
    }

    @Override
    public Resident update(Long id, Resident resident) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Resident found = em.find(Resident.class, id);
            if (found != null) {
                found.setRoomNumber(resident.getRoomNumber());
                found.setDateOfBirth(resident.getDateOfBirth());
                found.setMedicalConditions(resident.getMedicalConditions());
                found.setJournal(resident.getJournal());
                em.merge(found);
            }
            em.getTransaction().commit();
            return found;
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Resident resident = em.find(Resident.class, id);
            if (resident != null) {
                em.remove(resident);
            }
            em.getTransaction().commit();
        }
        return;
    }
}