package com.carebridge.controllers.impl;

import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerRoleTest {

    @Test
    void shouldTreatSubstituteAsCareworkerEquivalent() {
        User user = new User();
        user.setRole(Role.SUBSTITUTE);

        assertTrue(UserController.hasCareWorkerEquivalentRole(user));
    }

    @Test
    void shouldTreatCareworkerAsCareworkerEquivalent() {
        User user = new User();
        user.setRole(Role.CAREWORKER);

        assertTrue(UserController.hasCareWorkerEquivalentRole(user));
    }

    @Test
    void shouldNotTreatGuardianAsCareworkerEquivalent() {
        User user = new User();
        user.setRole(Role.GUARDIAN);

        assertFalse(UserController.hasCareWorkerEquivalentRole(user));
    }
}
