package dto;

public class AttachmentDTO {
    private int id;
    private String storagePath;
    private String mimeType;
    private String originalName;
    private long sizeBytes;
    private int journalEntryId;

    public AttachmentDTO() {}

    public AttachmentDTO(int id, String storagePath, String mimeType, String originalName, long sizeBytes, int journalEntryId) {
        this.id = id;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.originalName = originalName;
        this.sizeBytes = sizeBytes;
        this.journalEntryId = journalEntryId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public int getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(int journalEntryId) { this.journalEntryId = journalEntryId; }
}