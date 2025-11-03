package com.carebridge.dao;

import com.carebridge.models.JournalEntry;

public interface IJournalEntryDAO
{
    JournalEntry save(JournalEntry entry);
    JournalEntry findById(Long id);

}
