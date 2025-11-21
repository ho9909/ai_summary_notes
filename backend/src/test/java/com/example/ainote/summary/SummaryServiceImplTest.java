package com.example.ainote.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.repo.SummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SummaryServiceImplTest {

    @Autowired NoteService noteService;
    @Autowired SummaryService summaryService;
    @Autowired SummaryRepository summaryRepository;

    Long userId = 1L;
    Long noteId;

    @BeforeEach
    void setUp() {
        noteId = noteService.create(
                userId,
                new NoteDtos.Create("t", "# content line1\nline2", "ai")
        );
    }

    @Test
    void summarize_and_persist_cost_and_latest() {
        // when
        var resp = summaryService.summarize(userId, noteId, new SummaryService.Req("brief"));

        // then: 응답 검증
        assertThat(resp.oneLine()).isNotBlank();
        assertThat(resp.paragraph()).isNotBlank();
        assertThat(resp.tokensPrompt()).isPositive();
        assertThat(resp.tokensOutput()).isPositive();
        assertThat(resp.cost()).isInstanceOf(BigDecimal.class);

        // 그리고 DB 저장 수 확인 (Pageable 사용)
        var page = summaryRepository.findByNoteIdOrderByCreatedAtDesc(noteId, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        // latest()가 방금 생성된 것과 일치하는지
        var latest = summaryService.latest(userId, noteId);
        assertThat(latest).isNotNull();
        assertThat(latest.cost()).isEqualTo(resp.cost());
        assertThat(latest.model()).isEqualTo(resp.model());
        assertThat(latest.style()).isEqualTo(resp.style());
    }
}
