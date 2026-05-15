package com.carebridge.controllers.impl;

import com.carebridge.dtos.JwtUserDTO;
import com.carebridge.entities.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JournalEntryControllerRoleTest {

    @Test
    void shouldAllowFullJournalAccessWhenRoleIsSubstitute() {
        JwtUserDTO user = JwtUserDTO.builder()
                .roles(Set.of(Role.SUBSTITUTE.name()))
                .build();

        assertTrue(JournalEntryController.hasFullJournalAccess(user));
    }

    @Test
    void shouldAllowFullJournalAccessWhenRoleIsCareworker() {
        JwtUserDTO user = JwtUserDTO.builder()
                .roles(Set.of(Role.CAREWORKER.name()))
                .build();

        assertTrue(JournalEntryController.hasFullJournalAccess(user));
    }

    @Test
    void shouldAllowFullJournalAccessWhenRoleIsAdmin() {
        JwtUserDTO user = JwtUserDTO.builder()
                .roles(Set.of(Role.ADMIN.name()))
                .build();

        assertTrue(JournalEntryController.hasFullJournalAccess(user));
    }

    @Test
    void shouldNotAllowFullJournalAccessWhenRoleIsGuardian() {
        JwtUserDTO user = JwtUserDTO.builder()
                .roles(Set.of(Role.GUARDIAN.name()))
                .build();

        assertFalse(JournalEntryController.hasFullJournalAccess(user));
    }
}
