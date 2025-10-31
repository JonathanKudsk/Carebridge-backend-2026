package dto;

import java.time.LocalDateTime;
import java.util.List;

public class JournalEntryDTO {
    private int id;
    private int journalId;
    private String title;
    private String content;
    private int authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Integer> tagIds;

    public JournalEntryDTO() {}

    public JournalEntryDTO(int id, int journalId, String title, String content, int authorId,
                           LocalDateTime createdAt, LocalDateTime updatedAt, List<Integer> tagIds) {
        this.id = id;
        this.journalId = journalId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tagIds = tagIds;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }
}
