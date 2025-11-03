package dto;
import java.util.List;

public class ResidentDTO {
    private int id;
    private String name;
    private int guardianId;
    private List<int> careWorkerIds;
    private int journalId;

    public ResidentDTO() {}

    public ResidentDTO(int id, String name, int guardianId, List<int> careWorkerIds, int journalId) {
        this.id = id;
        this.name = name;
        this.guardianId = guardianId;
        this.careWorkerIds = careWorkerIds;
        this.journalId = journalId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }

    public List<int> getCareWorkerIds() { return careWorkerIds; }
    public void setCareWorkerIds(List<int> careWorkerIds) { this.careWorkerIds = careWorkerIds; }

    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }
}
