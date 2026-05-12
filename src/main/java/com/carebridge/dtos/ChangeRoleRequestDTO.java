package com.carebridge.dtos;

import com.carebridge.entities.enums.Role;

public class ChangeRoleRequestDTO {
    private Role role;

    public ChangeRoleRequestDTO() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}