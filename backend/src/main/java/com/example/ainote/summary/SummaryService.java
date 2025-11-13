package com.example.ainote.summary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface SummaryService {
    Resp summarize(Long userId, Long noteId, Req req);

    LatestResp latest(Long userId, Long noteId);

    Page<Item> list(Long userId, Long noteId, Pageable pageable);

    record Req(String style) {}

    // 비용 포함
    record Resp(String oneLine, String paragraph, String model, String style,
                int tokensPrompt, int tokensOutput, BigDecimal cost) {}

    record LatestResp(String oneLine, String paragraph, String model, String style,
                      int tokensPrompt, int tokensOutput, Instant createdAt, BigDecimal cost) {}

    record Item(Long id, String oneLine, String model, String style,
                int tokensPrompt, int tokensOutput, Instant createdAt, BigDecimal cost) {}
}
