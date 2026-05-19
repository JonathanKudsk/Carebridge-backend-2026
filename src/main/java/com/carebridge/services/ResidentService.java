package com.carebridge.services;

import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.services.mappers.ResidentMapper;

import java.util.List;

public class ResidentService {

    private final ResidentDAO residentDAO;

    public ResidentService() {
        this.residentDAO = ResidentDAO.getInstance();
    }

    public List<ResidentResponseDTO> getAllSorted(User currentUser) {
        List<Resident> residents;

        if (currentUser != null && currentUser.getRole() == Role.GUARDIAN) {
            residents = residentDAO.getAllSortedForGuardian(currentUser.getId());
        } else {
            residents = residentDAO.getAllSorted();
        }

        return residents
                .stream()
                .map(ResidentMapper::toDTO)
                .toList();
    }
}
