package dao;

import com.carebridge.dao.impl.JournalEntryDAO;
import com.carebridge.entities.JournalEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JournalEntryDAOTest {

    @Test
    public void testSearchReturnsDataWithoutCrashing() {
        // 1. Hent din DAO
        JournalEntryDAO dao = JournalEntryDAO.getInstance();

        // 2. Udfør en bred søgning (Ingen filtre, bare side 1 med op til 10 resultater)
        // Parametre: dateFrom, dateTo, employeeId, riskLevel, keyword, page, pageSize
        Object[] result = dao.search(null, null, null, null, null, 1, 10);

        // 3. Tjek at vi får et array tilbage
        assertNotNull(result, "Resultatet fra databasen må ikke være null");
        assertEquals(2, result.length, "Resultatet skal indeholde to elementer (data og total)");

        // 4. Udpak dataen
        @SuppressWarnings("unchecked")
        List<JournalEntry> entries = (List<JournalEntry>) result[0];
        Long total = (Long) result[1];

        // 5. Verificer (Bevis at det virker)
        assertNotNull(entries, "Listen af journal entries må ikke være null");
        assertNotNull(total, "Total tæller må ikke være null");
        assertTrue(total >= 0, "Total count skal være 0 eller større");

        // Udskriv til konsollen så du kan se at det virker i virkeligheden
        System.out.println("SUCCES! Søgningen virker. Fandt " + entries.size() + " entries ud af " + total
                + " totalt i databasen.");
    }
}