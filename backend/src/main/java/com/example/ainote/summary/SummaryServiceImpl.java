package com.example.ainote.summary;

import com.example.ainote.note.domain.Note;
import com.example.ainote.note.repo.NoteRepository;
import com.example.ainote.summary.domain.Summary;
import com.example.ainote.summary.repo.SummaryRepository;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final NoteRepository noteRepo;
    private final SummaryRepository summaryRepo;
    private final Summarizer summarizer;

    public SummaryServiceImpl(NoteRepository noteRepo, SummaryRepository summaryRepo, Summarizer summarizer) {
        this.noteRepo = noteRepo;
        this.summaryRepo = summaryRepo;
        this.summarizer = summarizer;
    }

    @Value("${ai.costs.prompt_per_1k:0.0}")
    private BigDecimal costPromptPer1K;

    @Value("${ai.costs.output_per_1k:0.0}")
    private BigDecimal costOutputPer1K;

    private BigDecimal calcCost(int promptTokens, int outputTokens) {
        BigDecimal p = costPromptPer1K.multiply(BigDecimal.valueOf(promptTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        BigDecimal o = costOutputPer1K.multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        return p.add(o);
    }

    @Override
    @Transactional
    public Resp summarize(Long userId, Long noteId, Req req) {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");
        if (note.getContentText() == null || note.getContentText().length() < 10)
            throw new IllegalArgumentException("content too short");

        String style = (req != null && req.style() != null && !req.style().isBlank()) ? req.style() : "brief";

        // 요약 호출
        var result = summarizer.summarize(note.getContentText(), style);
        String modelId = summarizer.modelId();

        // 저장
        Summary s = new Summary(
                note.getId(), modelId, style,
                result.oneLine(), result.paragraph(),
                result.tokensPrompt(), result.tokensOutput()
        );
        s.setCost(calcCost(result.tokensPrompt(), result.tokensOutput()));
        summaryRepo.save(s);

        return new Resp(
                s.getOneLine(),
                s.getParagraph(),
                s.getModel(),
                s.getStyle(),
                s.getTokensPrompt(),
                s.getTokensOutput(),
                s.getCost()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LatestResp latest(Long userId, Long noteId) {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");

        return summaryRepo.findTopByNoteIdOrderByCreatedAtDesc(noteId)
                .map(s -> new LatestResp(
                        s.getOneLine(),
                        s.getParagraph(),
                        s.getModel(),
                        s.getStyle(),
                        s.getTokensPrompt(),
                        s.getTokensOutput(),
                        s.getCreatedAt(),
                        s.getCost()
                ))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Item> list(Long userId, Long noteId, Pageable pageable) {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");

        return summaryRepo.findByNoteIdOrderByCreatedAtDesc(noteId, pageable)
                .map(s -> new Item(
                        s.getId(),
                        s.getOneLine(),
                        s.getModel(),
                        s.getStyle(),
                        s.getTokensPrompt(),
                        s.getTokensOutput(),
                        s.getCreatedAt(),
                        s.getCost()
                ));
    }
}
