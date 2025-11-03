package dto;

import java.time.LocalDateTime;
import java.util.List;

public class JournalEntryDTO {
    private int id;
    private int journalId;
    private JournalEntryType type; // enum
    private String content;
    private RiskLevel risk; // enum
    private String content;
    private int authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<int> tagIds;
    private List<int> checklistItemIds;
    private List<int> attachmentIds;

    public JournalEntryDTO() {}

     public JournalEntryDTO(int id, int journalId, String title, JournalEntryType type,
                           String content, RiskLevel risk, int authorId,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           List<int> tagIds, List<int> checklistItemIds, List<int> attachmentIds) {
        this.id = id;
        this.journalId = journalId;
        this.title = title;
        this.type = type;
        this.content = content;
        this.risk = risk;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tagIds = tagIds;
        this.checklistItemIds = checklistItemIds;
        this.attachmentIds = attachmentIds;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public JournalEntryType getType() { return type; }
    public void setType(JournalEntryType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public RiskLevel getRisk() { return risk; }
    public void setRisk(RiskLevel risk) { this.risk = risk; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<int> getTagIds() { return tagIds; }
    public void setTagIds(List<int> tagIds) { this.tagIds = tagIds; }

    public List<int> getChecklistItemIds() { return checklistItemIds; }
    public void setChecklistItemIds(List<int> checklistItemIds) { this.checklistItemIds = checklistItemIds; }

    public List<int> getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(List<int> attachmentIds) { this.attachmentIds = attachmentIds; }
}
