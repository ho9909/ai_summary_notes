package com.example.ainote.summary.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "summaries")
public class Summary {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "note_id", nullable = false)
  private Long noteId;

  @Column(nullable = false, length = 100)
  private String model; // 예: mock-1

  @Column(nullable = false, length = 20)
  private String style; // brief | detailed

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Lob
  @Column(name = "one_line", nullable = false, length = 300)
  private String oneLine;

  @Lob
  @Column(nullable = false)
  private String paragraph;

  @Column(name = "tokens_prompt", nullable = false)
  private int tokensPrompt;

  @Column(name = "tokens_output", nullable = false)
  private int tokensOutput;

  @Column(nullable = false, precision = 10, scale = 6)
  private BigDecimal cost = BigDecimal.ZERO;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Summary() {
  }

  public Summary(Long noteId, Long userId, String model, String style,
      String oneLine, String paragraph,
      int tokensPrompt, int tokensOutput) {
    this.noteId = noteId;
    this.userId = userId;
    this.model = model;
    this.style = style;
    this.oneLine = oneLine;
    this.paragraph = paragraph;
    this.tokensPrompt = tokensPrompt;
    this.tokensOutput = tokensOutput;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getNoteId() {
    return noteId;
  }

  public String getModel() {
    return model;
  }

  public String getStyle() {
    return style;
  }

  public String getOneLine() {
    return oneLine;
  }

  public String getParagraph() {
    return paragraph;
  }

  public int getTokensPrompt() {
    return tokensPrompt;
  }

  public int getTokensOutput() {
    return tokensOutput;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost == null ? BigDecimal.ZERO : cost;
  }
}