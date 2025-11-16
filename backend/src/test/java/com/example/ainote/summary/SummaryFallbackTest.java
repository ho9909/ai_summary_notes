package com.example.ainote.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.repo.SummaryRepository;
import com.example.ainote.summary.summary.Summarizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ai.fallbackOnError=true",   // 폴백 ON
        "ai.cacheMinutes=0"          // 캐시 무효화(테스트 간섭 방지)
})
@SpringBootTest(classes = { SummaryFallbackTest.TestFailingSummarizerConfig.class })
class SummaryFallbackTest {

    @TestConfiguration
    static class TestFailingSummarizerConfig {
        @Primary
        public Summarizer failingSummarizer() {
            return new Summarizer() {
                @Override public Result summarize(String plainText, String style) {
                    throw new RuntimeException("fail external provider");
                }
                @Override public String modelId() { return "broken"; }
            };
        }
    }

    @Autowired NoteService noteService;
    @Autowired SummaryService summaryService;
    @Autowired SummaryRepository summaryRepository;

    @Test
    void fallback_to_mock_when_external_fails() {
        Long uid = 1L;
        Long noteId = noteService.create(uid, new NoteDtos.Create("t", "# content for failure test", "ai"));

        var resp = summaryService.summarize(uid, noteId, new SummaryService.Req("detailed"));

        assertThat(resp.model()).isEqualTo("mock-1");
        assertThat(resp.cost()).isEqualTo(BigDecimal.ZERO);

        var latest = summaryService.latest(uid, noteId);
        assertThat(latest).isNotNull();
        assertThat(latest.model()).isEqualTo("mock-1");
        assertThat(latest.cost()).isEqualTo(BigDecimal.ZERO);
        assertThat(summaryRepository.findByNoteIdOrderByCreatedAtDesc(noteId).getTotalElements())
                .isEqualTo(1);
    }
}
