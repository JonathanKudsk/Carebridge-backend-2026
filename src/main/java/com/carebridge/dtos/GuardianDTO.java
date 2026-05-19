package com.carebridge.dtos;

public class GuardianDTO {
    private Long id;
    private String name;
    private String displayPhone;
    private String displayEmail;
    private String displayName;

    public GuardianDTO(Long id, String name, String displayPhone, String displayEmail, String displayName) {
        this.id = id;
        this.name = name;
        this.displayPhone = displayPhone;
        this.displayEmail = displayEmail;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayPhone() {
        return displayPhone;
    }
    public String getDisplayEmail() {
        return displayEmail;
    }

    public String getDisplayName() {
        return displayName;
    }
}
