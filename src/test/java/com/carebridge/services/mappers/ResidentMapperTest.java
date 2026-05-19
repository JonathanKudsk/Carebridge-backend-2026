package com.carebridge.services.mappers;

import com.carebridge.dtos.CreateResidentRequestDTO;
import com.carebridge.dtos.ResidentResponseDTO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResidentMapperTest {

    @Test
    void toDTOMapsResidentFieldsAndJournalId() {
        Resident resident = new Resident();
        resident.setId(10L);
        resident.setFirstName("Anna");
        resident.setLastName("Andersen");

        Journal journal = new Journal();
        journal.setId(20L);
        resident.setJournal(journal);

        ResidentResponseDTO dto = ResidentMapper.toDTO(resident);

        assertEquals(10L, dto.getId());
        assertEquals("Anna", dto.getFirstName());
        assertEquals("Andersen", dto.getLastName());
        assertEquals(20L, dto.getJournalId());
    }

    @Test
    void toDTOHandlesNullResident() {
        assertNull(ResidentMapper.toDTO(null));
    }

    @Test
    void toEntityMapsCreateResidentRequestFields() {
        CreateResidentRequestDTO dto = new CreateResidentRequestDTO(
                "Bent",
                "Berg",
                "020202-5678",
                null,
                null
        );

        Resident resident = ResidentMapper.toEntity(dto);

        assertEquals("Bent", resident.getFirstName());
        assertEquals("Berg", resident.getLastName());
        assertEquals("020202-5678", resident.getCprNr());
    }

    @Test
    void toEntityHandlesNullDto() {
        assertNull(ResidentMapper.toEntity(null));
    }
}
