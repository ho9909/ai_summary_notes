package com.example.ainote.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.repo.SummaryRepository;
import com.example.ainote.summary.summary.Summarizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ai.fallbackOnError=true",
        "ai.cacheMinutes=0",   // 캐시 영향 제거
        "ai.provider=mock"     // 기본은 mock(중앙 설정에서 한 개만 생성)
})
@SpringBootTest
class SummaryFallbackTest {

    @Autowired NoteService noteService;
    @Autowired SummaryService summaryService;
    @Autowired SummaryRepository summaryRepository;

    // 중앙 설정으로 생성된 Summarizer 빈을 테스트에서 Mock으로 교체
    @MockBean Summarizer summarizer;

    @Test
    void fallback_to_mock_when_external_fails() {
        // 메인 summarizer가 실패를 던지도록 설정
        when(summarizer.modelId()).thenReturn("broken");
        when(summarizer.summarize(anyString(), anyString()))
                .thenThrow(new RuntimeException("fail external provider"));

        Long uid = 1L;
        Long noteId = noteService.create(uid, new NoteDtos.Create("t", "# content for failure test", "ai"));

        var resp = summaryService.summarize(uid, noteId, new SummaryService.Req("detailed"));

        assertThat(resp.model()).isEqualTo("mock-1");          // 폴백 모델
        assertThat(resp.cost()).isEqualTo(BigDecimal.ZERO);    // 비용 0
        var latest = summaryService.latest(uid, noteId);
        assertThat(latest).isNotNull();
        assertThat(summaryRepository.countByNoteId(noteId)).isEqualTo(1);
    }
}
