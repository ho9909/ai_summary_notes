package com.example.ainote.summary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface SummaryService {

    /** 요청 파라미터 (요약 스타일) */
    record Req(String style) {}

    /** 요약 생성 결과 */
    record Resp(
            String oneLine,
            String paragraph,
            String model,
            String style,
            int tokensPrompt,
            int tokensOutput,
            BigDecimal cost
    ) {}

    /** 최신 1건 조회 결과 */
    record LatestResp(
            String oneLine,
            String paragraph,
            String model,
            String style,
            int tokensPrompt,
            int tokensOutput,
            Instant createdAt,
            BigDecimal cost
    ) {}

    /** 이력 페이지 아이템 */
    record Item(
            Long id,
            String oneLine,
            String model,
            String style,
            int tokensPrompt,
            int tokensOutput,
            Instant createdAt,
            BigDecimal cost
    ) {}

    Resp summarize(Long userId, Long noteId, Req req);

    LatestResp latest(Long userId, Long noteId);

    Page<Item> list(Long userId, Long noteId, Pageable pageable);
}
