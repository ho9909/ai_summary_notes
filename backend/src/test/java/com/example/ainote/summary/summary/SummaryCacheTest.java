package com.example.ainote.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.repo.SummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ai.cacheMinutes=10",        // TTL 10분
        "ai.fallbackOnError=true",   // 폴백 ON(무관)
        "ai.provider=mock"           // 테스트는 Mock 사용
})
@SpringBootTest
class SummaryCacheTest {

    @Autowired NoteService noteService;
    @Autowired SummaryService summaryService;
    @Autowired SummaryRepository summaryRepository;

    @Test
    void ttl_cache_reuses_latest_summary() {
        Long uid = 1L;
        Long noteId = noteService.create(uid, new NoteDtos.Create("t", "# content line1\nline2", "ai"));

        var r1 = summaryService.summarize(uid, noteId, new SummaryService.Req("brief"));
        var countAfterFirst = summaryRepository.findByNoteIdOrderByCreatedAtDesc(noteId).getTotalElements();

        var r2 = summaryService.summarize(uid, noteId, new SummaryService.Req("brief"));
        var page = summaryRepository.findByNoteIdOrderByCreatedAtDesc(noteId);

        assertThat(countAfterFirst).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(1);      // 두 번째도 DB 쓰기 없이 재사용
        assertThat(r2.oneLine()).isEqualTo(r1.oneLine());      // 동일 결과 반환(캐시 히트)
    }
}
