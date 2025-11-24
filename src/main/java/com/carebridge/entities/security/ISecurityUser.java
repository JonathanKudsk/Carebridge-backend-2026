package com.carebridge.entities.security;

import com.carebridge.entities.enums.Role;

public interface ISecurityUser {
    boolean verifyPassword(String pw);

    void addRole(Role role);

    void removeRole(String role);
}
