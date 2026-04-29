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
public class JournalDAOTest {

    @Autowired
    private JournalDAO journalDAO;
    @Autowired
    private JournalEntryDAO entryDAO;
    @Autowired
    private ResidentDAO residentDAO;
    @Autowired
    private UserDAO userDAO;

    private User testUser;
    private Resident testResident;
    private Long journalId;
    private Long entryId;

    @BeforeEach
    void setUp() {
        testUser = new User("Journal User", "juser" + System.nanoTime() + "@test.com", "pass", Role.USER);
        userDAO.create(testUser);

        testResident = new Resident();
        testResident.setFirstName("Resident");
        testResident.setLastName("One");
        testResident.setCprNr("RES" + System.nanoTime());
        residentDAO.create(testResident);
        
        Journal journal = new Journal();
        journal.setResident(testResident);
        Journal createdJournal = journalDAO.create(journal);
        journalId = createdJournal.getId();

        JournalEntry entry = new JournalEntry(testUser, "Title", "Content", RiskAssessment.LOW, EntryType.DAILY);
        entry.setJournal(createdJournal);
        JournalEntry createdEntry = entryDAO.create(entry);
        entryId = createdEntry.getId();
        
        createdJournal.addEntry(createdEntry);
    }

    @Test
    @Order(1)
    void testReadJournal() {
        assertNotNull(journalDAO.read(journalId));
        assertFalse(journalDAO.readAll().isEmpty());
    }

    @Test
    @Order(2)
    void testUpdateDeleteBranches() {
        assertNotNull(journalDAO.update(journalId, new Journal()));
        assertThrows(ApiRuntimeException.class, () -> journalDAO.update(999999L, new Journal()));
        
        journalDAO.delete(journalId);
        assertNull(journalDAO.read(journalId));
        
        // delete null check
        journalDAO.delete(999999L);
    }

    @Test
    @Order(3)
    void testAddEntryToJournal() {
        Journal j = journalDAO.read(journalId);
        JournalEntry e = new JournalEntry(testUser, "T2", "C2", RiskAssessment.LOW, EntryType.NOTE);
        entryDAO.create(e);
        
        journalDAO.addEntryToJournal(j, e);
        
        Journal read = journalDAO.read(journalId);
        assertTrue(read.getEntries().size() >= 2);
    }
}
