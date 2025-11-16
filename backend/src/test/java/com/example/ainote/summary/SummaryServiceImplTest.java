package com.example.ainote.summary;

import com.example.ainote.note.NoteService;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.summary.repo.SummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SummaryServiceImplTest {

    @Autowired NoteService notes;
    @Autowired SummaryService sums;
    @Autowired SummaryRepository repo;

    Long userId = 1L;
    Long noteId;

    @BeforeEach
    void setup() {
        var id = notes.create(userId, new NoteDtos.Create("t","# content line1\nline2","ai"));
        noteId = id;
    }

    @Test
    void summarize_and_cost_persisted() {
        var resp = sums.summarize(userId, noteId, new SummaryService.Req("brief"));
        assertThat(resp.oneLine()).isNotBlank();
        assertThat(resp.tokensPrompt()).isPositive();
        assertThat(resp.tokensOutput()).isPositive();
        assertThat(resp.cost()).isInstanceOf(BigDecimal.class);

        var latest = sums.latest(userId, noteId);
        assertThat(latest).isNotNull();
        assertThat(latest.cost()).isEqualTo(resp.cost());
        assertThat(repo.findByNoteIdOrderByCreatedAtDesc(noteId).getContent())
                .hasSize(1);
    }
}
