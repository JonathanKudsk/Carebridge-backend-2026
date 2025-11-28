package com.carebridge.dtos;

import com.carebridge.entities.enums.Role;

public class RegisterUserDTO {
    private String name;
    private String email;
    private String password;
    private String displayName;
    private String displayEmail;
    private String displayPhone;
    private String internalEmail;
    private String internalPhone;
    private Role role;

    public RegisterUserDTO() {}

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDisplayEmail() { return displayEmail; }
    public void setDisplayEmail(String displayEmail) { this.displayEmail = displayEmail; }

    public String getDisplayPhone() { return displayPhone; }
    public void setDisplayPhone(String displayPhone) { this.displayPhone = displayPhone; }

    public String getInternalEmail() { return internalEmail; }
    public void setInternalEmail(String internalEmail) { this.internalEmail = internalEmail; }

    public String getInternalPhone() { return internalPhone; }
    public void setInternalPhone(String internalPhone) { this.internalPhone = internalPhone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
