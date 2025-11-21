package com.example.ainote.summary.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.SummaryService;
import com.example.ainote.summary.repo.SummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ai.cacheMinutes=10",
        "ai.fallbackOnError=true",
        "ai.provider=mock"
})
@SpringBootTest
class SummaryCacheTest {

    @Autowired NoteService noteService;
    @Autowired SummaryService summaryService;
    @Autowired SummaryRepository summaryRepository;

    @Test
    void ttl_cache_reuses_latest_summary() {
        Long uid = 1L;
        Long noteId = noteService.create(uid,
                new NoteDtos.Create("t", "# content line1\nline2", "ai"));

        var r1 = summaryService.summarize(uid, noteId, new SummaryService.Req("brief"));

        var countAfterFirst = summaryRepository
                .findByNoteIdOrderByCreatedAtDesc(noteId, PageRequest.of(0, 10))
                .getTotalElements();

        var r2 = summaryService.summarize(uid, noteId, new SummaryService.Req("brief"));

        var page = summaryRepository
                .findByNoteIdOrderByCreatedAtDesc(noteId, PageRequest.of(0, 10));

        assertThat(countAfterFirst).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(1);      // 두 번째 요청도 DB 추가 저장 없음(캐시 히트)
        assertThat(r2.oneLine()).isEqualTo(r1.oneLine());      // 동일 결과 재사용
    }
}
