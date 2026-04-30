package com.carebridge.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

@Table(name = "journal_entry_Answers")
public class JournalEntryAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name="answer")
    private String answer;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;
}
