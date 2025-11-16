package com.example.ainote.note.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notes")
public class Note {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, length=200)
    private String title;

    @Lob @Column(name="content_md", nullable=false)
    private String contentMd;

    @Lob @Column(name="content_text", nullable=false)
    private String contentText;

    @Column(nullable=false, length=20)
    private String status = "DRAFT";

    @Column(length = 200)
    private String tags = ""; // "ai,spring" 형태

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt = Instant.now();

    protected Note() {}

    public Note(Long userId, String title, String contentMd) {
        this.userId = userId;
        this.title = title;
        updateContent(contentMd);
    }

    public void updateContent(String newContentMd) {
        if (newContentMd == null) return;
        this.contentMd = newContentMd;
        this.contentText = newContentMd
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("\\*\\*?(.*?)\\*\\*?", "$1")
                .replaceAll("`{1,3}(.*?)`{1,3}", "$1")
                .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1")
                .replaceAll("\\r?\\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (this.contentText.isEmpty()) this.contentText = "";
        this.updatedAt = Instant.now();
    }
    public void setTitle(String title) { if (title != null) this.title = title; this.updatedAt = Instant.now(); }
    public void setStatus(String status) { if (status != null) this.status = status; this.updatedAt = Instant.now(); }
    public void setTags(String tags) {
        if (tags == null) return;
        this.tags = tags.replaceAll("\\s+", ""); // 공백 제거
        this.updatedAt = Instant.now();
    }
    @PrePersist public void prePersist() {
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
        if (this.contentText == null) this.contentText = "";
        if (this.tags == null) this.tags = "";
    }
    @PreUpdate public void preUpdate() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContentMd() { return contentMd; }
    public String getContentText() { return contentText; }
    public String getStatus() { return status; }
    public String getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
