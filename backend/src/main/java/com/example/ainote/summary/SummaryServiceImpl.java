package com.example.ainote.summary;

import com.example.ainote.note.domain.Note;
import com.example.ainote.note.repo.NoteRepository;
import com.example.ainote.summary.domain.Summary;
import com.example.ainote.summary.repo.SummaryRepository;
import com.example.ainote.summary.summary.MockSummarizer;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

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

        @Value("${ai.fallbackOnError:true}")
        private boolean fallbackOnError;

        @Value("${ai.cacheMinutes:10}")
        private int cacheMinutes;

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

                // ✅ 요약에 사용할 평문 선택 (contentText 우선, 없으면 contentMd)
                String plain = note.getContentText() != null ? note.getContentText() : note.getContentMd();
                if (plain == null || plain.trim().length() < 10) {
                        throw new IllegalArgumentException("content too short");
                }

                // ✅ 스타일 정리
                String style = (req != null && req.style() != null && !req.style().isBlank())
                                ? req.style().trim()
                                : "brief";

                // 1) DB 최신 요약 TTL 캐시(노트ID+스타일 기준, null-safe)
                if (cacheMinutes > 0) {
                        var latestOpt = summaryRepo.findTopByNoteIdOrderByCreatedAtDesc(noteId);
                        if (latestOpt.isPresent()) {
                                var last = latestOpt.get();
                                boolean sameStyle = style.equalsIgnoreCase(last.getStyle());
                                Instant createdAt = last.getCreatedAt();
                                boolean withinTtl = createdAt != null &&
                                                Duration.between(createdAt, Instant.now()).toMinutes() < cacheMinutes;
                                if (sameStyle && withinTtl) {
                                        return new Resp(
                                                        last.getOneLine(),
                                                        last.getParagraph(),
                                                        last.getModel(),
                                                        last.getStyle(),
                                                        last.getTokensPrompt(),
                                                        last.getTokensOutput(),
                                                        last.getCost());
                                }
                        }
                }

                // 2) 외부 요약 호출 + 저장
                try {
                        var result = summarizer.summarize(plain, style);
                        String modelId = summarizer.modelId();

                        Summary s = new Summary(
                                        note.getId(), userId, modelId, style,
                                        result.oneLine(), result.paragraph(),
                                        result.tokensPrompt(), result.tokensOutput());
                        s.setCost(calcCost(result.tokensPrompt(), result.tokensOutput()));
                        summaryRepo.save(s);

                        return new Resp(
                                        s.getOneLine(), s.getParagraph(), s.getModel(), s.getStyle(),
                                        s.getTokensPrompt(), s.getTokensOutput(), s.getCost());

                } catch (Exception ex) {
                        // 3) 폴백: 옵션이 켜져 있으면 MockSummarizer로 대체
                        if (!fallbackOnError)
                                throw ex;

                        var mock = new MockSummarizer();
                        var result = mock.summarize(plain, style);
                        Summary s = new Summary(
                                        note.getId(), userId, "gemini", style,
                                        result.oneLine(), result.paragraph(),
                                        result.tokensPrompt(), result.tokensOutput());
                        s.setCost(BigDecimal.ZERO);
                        summaryRepo.save(s);

                        return new Resp(
                                        s.getOneLine(), s.getParagraph(), s.getModel(), s.getStyle(),
                                        s.getTokensPrompt(), s.getTokensOutput(), s.getCost());
                }
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
                                                s.getOneLine(), s.getParagraph(), s.getModel(), s.getStyle(),
                                                s.getTokensPrompt(), s.getTokensOutput(), s.getCreatedAt(),
                                                s.getCost()))
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
                                                s.getId(), s.getOneLine(), s.getModel(), s.getStyle(),
                                                s.getTokensPrompt(), s.getTokensOutput(), s.getCreatedAt(),
                                                s.getCost()));
        }
}
