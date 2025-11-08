package com.example.ainote.note;

import com.example.ainote.note.domain.Note;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.note.repo.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository repo;

    public NoteServiceImpl(NoteRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Long create(Long userId, NoteDtos.Create req) {
        Note n = new Note();
        n.setUserId(userId);
        n.setTitle(req.title());
        n.setContentMd(req.contentMd());
        n.setContentText(stripMarkdown(req.contentMd()));
        n.setStatus("DRAFT");
        return repo.save(n).getId();
    }

    @Override
    public NoteDtos.Response get(Long userId, Long id) {
        Note n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!n.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");
        return toDto(n);
    }

    @Override
    public Page<NoteDtos.Response> list(Long userId, String query, Pageable pageable) {
        String q = (query == null) ? "" : query;
        return repo.findByUserIdAndTitleContainingIgnoreCase(userId, q, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public void update(Long userId, Long id, NoteDtos.Update req) {
        Note n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!n.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");
        if (req.title() != null && !req.title().isBlank())
            n.setTitle(req.title());
        if (req.contentMd() != null && !req.contentMd().isBlank()) {
            n.setContentMd(req.contentMd());
            n.setContentText(stripMarkdown(req.contentMd()));
        }
        if (req.status() != null && !req.status().isBlank())
            n.setStatus(req.status());
        repo.save(n);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Note n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!n.getUserId().equals(userId))
            throw new IllegalArgumentException("forbidden");
        repo.delete(n);
    }

    private NoteDtos.Response toDto(Note n) {
        return new NoteDtos.Response(n.getId(), n.getTitle(), n.getContentMd(), n.getContentText(), n.getStatus());
    }

    // 매우 ?�순??마크?�운 ?�거(?�리코스 ?��?)
    private String stripMarkdown(String md) {
        if (md == null)
            return "";
        // ?�더/리스??기호�?간단 ?�거
        String t = md.replaceAll("[#*_`>\\-\\+]", "");
        // 링크/?��?지 문법 ?��??�거
        t = t.replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "")
                .replaceAll("\\[[^\\]]*\\]\\([^)]*\\)", "");
        // 코드블록 백틱 ?�거
        t = t.replaceAll("```[\\s\\S]*?```", " ");
        return t.trim();
    }
}
