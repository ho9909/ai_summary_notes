package com.example.ainote.summary;

import com.example.ainote.note.domain.Note;
import com.example.ainote.note.repo.NoteRepository;
import com.example.ainote.summary.domain.Summary;
import com.example.ainote.summary.repo.SummaryRepository;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Resp summarize(Long userId, Long noteId, Req req) {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");
        if (note.getContentText() == null || note.getContentText().length() < 10)
            throw new IllegalArgumentException("content too short");

        String style = (req != null && req.style() != null && !req.style().isBlank())
                ? req.style() : "brief";

        var result = summarizer.summarize(note.getContentText(), style);

        Summary summary = new Summary(
                note.getId(),
                "mock-1",
                style,
                result.oneLine(),
                result.paragraph(),
                result.tokensPrompt(),
                result.tokensOutput()
        );
        summaryRepo.save(summary);

        return new Resp(
                summary.getOneLine(),
                summary.getParagraph(),
                summary.getModel(),
                summary.getStyle(),
                summary.getTokensPrompt(),
                summary.getTokensOutput()
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
                        s.getOneLine(), s.getParagraph(), s.getModel(), s.getStyle(),
                        s.getTokensPrompt(), s.getTokensOutput(), s.getCreatedAt()
                ))
                .orElse(null); // 컨트롤러에서 204 처리
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
                        s.getTokensPrompt(), s.getTokensOutput(), s.getCreatedAt()
                ));
    }
}
