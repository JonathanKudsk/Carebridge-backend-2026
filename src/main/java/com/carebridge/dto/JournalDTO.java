package dto;

import java.util.List;

public class JournalDTO {
    private int id;
    private int residentId;
    private List<int> entryIds;

    public JournalDTO() {}

    public JournalDTO(int id, int residentId, List<int> entryIds) {
        this.id = id;
        this.residentId = residentId;
        this.entryIds = entryIds;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }
    public List<int> getEntryIds() { return entryIds; }
    public void setEntryIds(List<int> entryIds) { this.entryIds = entryIds; }
}
