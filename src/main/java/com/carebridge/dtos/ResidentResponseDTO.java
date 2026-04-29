package com.carebridge.dtos;

public class ResidentResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Long journalId;
    private Long medicationChartId;
    private String cprNr;
    private Integer age;
    private String gender;

    public ResidentResponseDTO() {}

    public ResidentResponseDTO(Long id, String firstName, String lastName, Long journalId, Long medicationChartId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.journalId = journalId;
        this.medicationChartId = medicationChartId;
    }

    public ResidentResponseDTO(Long id, String firstName, String lastName, String cprNr, Integer age, String gender, Long journalId, Long medicationChartId){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.journalId = journalId;
        this.medicationChartId = medicationChartId;
        this.cprNr = cprNr;
        this.age = age;
        this.gender = gender;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Long getJournalId() { return journalId; }
    public Long getMedicationChartId() { return medicationChartId;}
    public String getCprNr() { return cprNr; }
    public Integer getAge() { return age; }
    public String getGender() { return gender; }

}
