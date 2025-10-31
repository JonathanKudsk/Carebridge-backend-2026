package entity;

import entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @Enumerated(EnumType.STRING)
    private JournalType type;

    @Enumerated(EnumType.STRING)
    private RiskLevel risk;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int authorId; // Kan være CareWorker eller Admin

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Mange JournalEntries → 1 Journal
    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;

    // 1 Entry → mange Tags
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
    private List<Tag> tags;

    // 1 Entry → mange ChecklistItems
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
    private List<ChecklistItem> checklistItems;

    // 1 Entry → mange Attachments
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
    private List<Attachment> attachments;

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public JournalType getType() { return type; }
    public void setType(JournalType type) { this.type = type; }
    public RiskLevel getRisk() { return risk; }
    public void setRisk(RiskLevel risk) { this.risk = risk; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Journal getJournal() { return journal; }
    public void setJournal(Journal journal) { this.journal = journal; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public List<ChecklistItem> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<ChecklistItem> checklistItems) { this.checklistItems = checklistItems; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(List<Attachment> attachments) { this.attachments = attachments; }
}
