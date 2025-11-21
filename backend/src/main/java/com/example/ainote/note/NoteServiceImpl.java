package com.example.ainote.note;

import com.example.ainote.note.domain.Note;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.note.repo.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository repo;

    public NoteServiceImpl(NoteRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> list(Long userId, String query, String tag, Pageable pageable) {
        String q = query != null ? query.trim() : null;
        String t = tag   != null ? tag.trim().toLowerCase() : null;
        boolean hasQ = q != null && !q.isEmpty();
        boolean hasT = t != null && !t.isEmpty();

        Page<Note> page;
        if (hasQ && hasT) {
            page = repo.findByUserIdAndTitleLikeAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(
                    userId, q, t, pageable);
        } else if (hasT) {
            page = repo.findByUserIdAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(
                    userId, t, pageable);
        } else if (hasQ) {
            page = repo.findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    userId, q, pageable);
        } else {
            page = repo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> listByTag(Long userId, String tag, Pageable pageable) {
        String t = tag != null ? tag.trim().toLowerCase() : null;
        return repo.findByUserIdAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(userId, t, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> listByQueryAndTag(Long userId, String query, String tag, Pageable pageable) {
        String q = query != null ? query.trim() : null;
        String t = tag   != null ? tag.trim().toLowerCase() : null;
        return repo.findByUserIdAndTitleLikeAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(
                userId, q, t, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public Long create(Long userId, NoteDtos.Create req) {
        Note note = new Note(userId, req.title(), req.contentMd());
        note.setTags(normalizeTags(req.tags())); // ✅ 태그 정규화
        repo.save(note);
        return note.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDtos.Response detail(Long userId, Long id) {
        Note note = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) throw new IllegalArgumentException("forbidden");
        return toResponse(note);
    }

    @Override
    @Transactional
    public void update(Long userId, Long id, NoteDtos.Update req) {
        Note note = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) throw new IllegalArgumentException("forbidden");

        if (req.title() != null)     note.setTitle(req.title().trim());
        if (req.contentMd() != null) note.updateContent(req.contentMd());
        if (req.status() != null)    note.setStatus(req.status().trim()); // enum이면 변환해서 set
        if (req.tags() != null)      note.setTags(normalizeTags(req.tags())); // ✅ 태그 정규화
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Note note = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) throw new IllegalArgumentException("forbidden");
        repo.delete(note);
    }

    private String normalizeTags(String raw) {
        if (raw == null) return "";
        String[] parts = raw.split(",");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String p : parts) {
            String t = p.trim().toLowerCase();
            if (!t.isEmpty()) set.add(t);
        }
        return String.join(",", set);
    }

    private NoteDtos.Response toResponse(Note n) {
        return new NoteDtos.Response(
                n.getId(),
                n.getTitle(),
                n.getContentMd(),
                n.getContentText(),
                n.getStatus(), // enum이면 .name()
                n.getTags()
        );
    }
}
