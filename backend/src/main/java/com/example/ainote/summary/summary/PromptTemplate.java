package com.example.ainote.summary.summary;

public class PromptTemplate {

  /** 시스템 프롬프트(역할/톤/출력 규칙) */
  public static String system() {
    return """
      You are a helpful note-taking summarizer.
      - If the input is mainly Korean, answer in Korean.
      - Be concise and factual.
      - Avoid hallucinations and don't invent facts.
      - Keep output safe for work.
      """;
  }

  /** 유저 프롬프트(스타일/본문 포함) */
  public static String user(String style, String contentText) {
    String s = (style == null || style.isBlank()) ? "brief" : style.toLowerCase();
    String guide = switch (s) {
      case "detailed" -> """
        Summarize the content as 3-5 bullet points.
        - Use short bullets.
        - Focus on key facts & actions.
        """;
      default -> """
        Summarize the content as a single short paragraph.
        - The first sentence must be <= 80 chars.
        """;
    };
    return guide + "\n\nCONTENT:\n" + safe(contentText);
  }

  private static String safe(String s) {
    if (s == null) return "";
    // 아주 가벼운 정규화
    return s.replaceAll("\\s+", " ").trim();
  }
}
