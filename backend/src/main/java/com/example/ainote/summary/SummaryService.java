package com.example.ainote.summary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface SummaryService {
    // D5: 생성
    Resp summarize(Long userId, Long noteId, Req req);

    // ✅ D6: 최신 1개 조회
    LatestResp latest(Long userId, Long noteId);

    // ✅ D6: 이력 목록 조회(페이징)
    Page<Item> list(Long userId, Long noteId, Pageable pageable);

    // ---- DTOs ----
    record Req(String style) {}
    record Resp(String oneLine, String paragraph, String model, String style,
                int tokensPrompt, int tokensOutput) {}

    // 최신 1개 응답 (createdAt 포함)
    record LatestResp(String oneLine, String paragraph, String model, String style,
                      int tokensPrompt, int tokensOutput, Instant createdAt) {}

    // 목록 아이템(요약들)
    record Item(Long id, String oneLine, String model, String style,
                int tokensPrompt, int tokensOutput, Instant createdAt) {}
}
