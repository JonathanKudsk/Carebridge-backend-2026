package com.carebridge.dtos;

public class CreateResidentRequestDTO {
    private String firstName;
    private String lastName;
    private String cprNr;

    public CreateResidentRequestDTO() {}

    public CreateResidentRequestDTO(String firstName, String lastName, String cprNr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNr = cprNr;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCprNr() { return cprNr; }
    public void setCprNr(String cprNr) { this.cprNr = cprNr; }
}
