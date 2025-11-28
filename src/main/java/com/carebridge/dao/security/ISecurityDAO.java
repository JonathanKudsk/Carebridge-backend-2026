package com.carebridge.dao.security;


import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ValidationException;

public interface ISecurityDAO {
    User getVerifiedUser(String email, String password) throws ValidationException;

    User createUser(
            String name,
            String email,
            String rawPassword,
            String displayName,
            String displayEmail,
            String displayPhone,
            String internalEmail,
            String internalPhone,
            Role role
    );
    User changeRole(Long userId, com.carebridge.entities.enums.Role newRole);
}
