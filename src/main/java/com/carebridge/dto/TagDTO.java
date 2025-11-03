package dto;

public class TagDTO {
    private int id;
    private String value;
    private int journalEntryId;

    public TagDTO() {}

    public TagDTO(int id, String value, int journalEntryId) {
        this.id = id;
        this.value = value;
        this.journalEntryId = journalEntryId;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(int journalEntryId) { this.journalEntryId = journalEntryId; }
}
