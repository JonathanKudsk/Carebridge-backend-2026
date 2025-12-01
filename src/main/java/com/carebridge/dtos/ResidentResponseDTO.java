package com.carebridge.dtos;

public class ResidentResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Long journalId;

    public ResidentResponseDTO() {}

    public ResidentResponseDTO(Long id, String firstName, String lastName, Long journalId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.journalId = journalId;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Long getJournalId() { return journalId; }
}