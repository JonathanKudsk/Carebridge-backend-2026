package com.carebridge.dtos;

import java.time.Instant;
import java.util.List;

public class ResidentDetailsResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String cprNr;
    private Integer age;
    private String gender;
    private boolean isActive;

    private Long journalId;
    private Long medicationChartId;

    private Long userId;
    private String userDisplayName;
    private String displayPhone;
    private String displayEmail;
    private Instant createdAt;
    private Instant updatedAt;
    private List<GuardianDTO> guardians;

    public ResidentDetailsResponseDTO() {
    }

    public ResidentDetailsResponseDTO(
            Long id,
            String firstName,
            String lastName,
            String cprNr,
            Integer age,
            String gender,
            boolean isActive,
            Long journalId,
            Long medicationChartId,
            Long userId,
            String userDisplayName,
            String displayPhone,
            String displayEmail,
            Instant createdAt,
            Instant updatedAt,
            List<GuardianDTO> guardians
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNr = cprNr;
        this.age = age;
        this.gender = gender;
        this.isActive = isActive;
        this.journalId = journalId;
        this.medicationChartId = medicationChartId;
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.displayPhone = displayPhone;
        this.displayEmail = displayEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.guardians = guardians;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCprNr() {
        return cprNr;
    }

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public boolean isActive() {
        return isActive;
    }

    public Long getJournalId() {
        return journalId;
    }

    public Long getMedicationChartId() {
        return medicationChartId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getDisplayPhone() {
        return displayPhone;
    }

    public String getDisplayEmail() {
        return displayEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<GuardianDTO> getGuardians() {
        return guardians;
    }

    public void setGuardians(List<GuardianDTO> guardians) {
        this.guardians = guardians;
    }
}