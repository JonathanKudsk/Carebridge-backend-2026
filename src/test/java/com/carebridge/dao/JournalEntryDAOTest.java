package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.JournalDAO;
import com.carebridge.dao.impl.JournalEntryDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JournalEntryDAOTest {

    @Autowired
    private JournalEntryDAO entryDAO;
    @Autowired
    private JournalDAO journalDAO;
    @Autowired
    private ResidentDAO residentDAO;
    @Autowired
    private UserDAO userDAO;

    private Long entryId;
    private Long journalId;

    @BeforeEach
    void setUp() {
        User user = new User("U", "u" + System.nanoTime() + "@t.com", "p", Role.USER);
        userDAO.create(user);
        Resident r = new Resident(); r.setFirstName("R"); r.setCprNr("C" + System.nanoTime());
        residentDAO.create(r);
        Journal j = new Journal(); j.setResident(r);
        journalDAO.create(j);
        journalId = j.getId();
        
        JournalEntry entry = new JournalEntry(user, "T", "C", RiskAssessment.LOW, EntryType.NOTE);
        entry.setJournal(j);
        entryDAO.create(entry);
        entryId = entry.getId();
    }

    @Test
    @Order(1)
    void testUpdateBranches() {
        JournalEntry patch = new JournalEntry();
        patch.setContent("NewC");
        patch.setTitle("NewT");
        JournalEntry updated = entryDAO.update(entryId, patch);
        assertEquals("NewC", updated.getContent());
        assertEquals("NewT", updated.getTitle());

        // Null branches
        JournalEntry patch2 = new JournalEntry();
        patch2.setContent(null);
        patch2.setTitle(null);
        JournalEntry updated2 = entryDAO.update(entryId, patch2);
        assertEquals("NewC", updated2.getContent());
        assertEquals("NewT", updated2.getTitle());
        
        assertThrows(ApiRuntimeException.class, () -> entryDAO.update(999999L, new JournalEntry()));
    }

    @Test
    @Order(2)
    void testDeleteAndRead() {
        assertNotNull(entryDAO.read(entryId));
        assertFalse(entryDAO.readAll().isEmpty());
        assertFalse(entryDAO.getEntryIdsByJournalId(journalId).isEmpty());
        
        entryDAO.delete(entryId);
        assertNull(entryDAO.read(entryId));
        
        // delete null check
        entryDAO.delete(999999L);
    }
}
