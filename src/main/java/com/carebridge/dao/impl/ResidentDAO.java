package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class ResidentDAO implements IDAO<Resident, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Resident create(Resident resident) {
        if (resident == null) throw new ApiRuntimeException(400, "Resident cannot be null");
        em.persist(resident);
        return resident;
    }

    @Override
    public Resident read(Long id) {
        return em.find(Resident.class, id);
    }

    public Resident readByCpr(String cpr) {
        try {
            return em.createQuery("SELECT r FROM Resident r WHERE r.cprNr = :cpr", Resident.class)
                    .setParameter("cpr", cpr)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Resident> readAll() {
        return em.createQuery("SELECT r FROM Resident r", Resident.class).getResultList();
    }

    @Override
    public Resident update(Long id, Resident patch) {
        Resident managed = read(id);
        if (managed == null) return null;
        if (patch.getFirstName() != null) managed.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) managed.setLastName(patch.getLastName());
        if (patch.getCprNr() != null) managed.setCprNr(patch.getCprNr());
        return managed;
    }

    @Override
    public void delete(Long id) {
        Resident managed = em.find(Resident.class, id);
        if (managed != null) {
            em.remove(managed);
        }
    }
}
