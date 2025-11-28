package com.carebridge.dtos;

public class CreateResidentRequestDTO {
    private String firstName;
    private String lastName;
    private String cprNr;
    private String userId;
    private String guardianId;

    public CreateResidentRequestDTO() {}

    public CreateResidentRequestDTO(String firstName, String lastName, String cprNr, String userId, String guardianId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNr = cprNr;
        this.userId = userId;
        this.guardianId = guardianId;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCprNr() { return cprNr; }
    public void setCprNr(String cprNr) { this.cprNr = cprNr; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGuardianId() { return guardianId; }
    public void setGuardianId(String guardianId) { this.guardianId = guardianId; }
}
