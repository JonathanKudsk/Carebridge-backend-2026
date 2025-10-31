package dto;

import java.util.List;

public class ResidentDTO {
    private int id;
    private String name;
    private Integer guardianId; // simplere reference
    private List<Integer> careWorkerIds;
    private Integer journalId;

    public ResidentDTO() {}

    public ResidentDTO(int id, String name, Integer guardianId, List<Integer> careWorkerIds, Integer journalId) {
        this.id = id;
        this.name = name;
        this.guardianId = guardianId;
        this.careWorkerIds = careWorkerIds;
        this.journalId = journalId;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getGuardianId() { return guardianId; }
    public void setGuardianId(Integer guardianId) { this.guardianId = guardianId; }
    public List<Integer> getCareWorkerIds() { return careWorkerIds; }
    public void setCareWorkerIds(List<Integer> careWorkerIds) { this.careWorkerIds = careWorkerIds; }
    public Integer getJournalId() { return journalId; }
    public void setJournalId(Integer journalId) { this.journalId = journalId; }
}
