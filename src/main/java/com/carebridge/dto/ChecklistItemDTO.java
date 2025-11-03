package dto;

public class ChecklistItemDTO {
    private int id;
    private String text;
    private ChecklistStatus status;
    private int journalEntryId;

    public ChecklistItemDTO() {}

    public ChecklistItemDTO(int id, String text, ChecklistStatus status, int journalEntryId) {
        this.id = id;
        this.text = text;
        this.status = status;
        this.journalEntryId = journalEntryId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public ChecklistStatus getStatus() { return status; }
    public void setStatus(ChecklistStatus status) { this.status = status; }

    public int getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(int journalEntryId) { this.journalEntryId = journalEntryId; }
}
