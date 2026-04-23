package com.carebridge.services;

import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Resident;
import com.carebridge.services.mappers.ResidentMapper;

import java.util.List;

public class ResidentService {

    private final ResidentDAO residentDAO;

    public ResidentService() {
        this.residentDAO = ResidentDAO.getInstance();
    }

    public List<ResidentResponseDTO> getAllSorted() {
        return residentDAO.getAllSorted()
                .stream()
                .map(ResidentMapper::toDTO)
                .toList();
    }
}