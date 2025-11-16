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
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> list(Long userId, String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                    .map(this::toResponse);
        }
        return repo.findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(userId, query, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> listByTag(Long userId, String tag, Pageable pageable) {
        return repo.findByUserIdAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(userId, tag, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDtos.Response> listByQueryAndTag(Long userId, String query, String tag, Pageable pageable) {
        return repo.findByUserIdAndTitleContainingIgnoreCaseAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(
                        userId, query, tag, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Long create(Long userId, NoteDtos.Create req) {
        Note note = new Note(userId, req.title(), req.contentMd());
        if (req.tags() != null) note.setTags(req.tags());
        repo.save(note);
        return note.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDtos.Response detail(Long userId, Long id) {
        Note note = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        return toResponse(note);
    }

    @Override
    @Transactional
    public void update(Long userId, Long id, NoteDtos.Update req) {
        Note note = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        if (req.title() != null) note.setTitle(req.title());
        if (req.contentMd() != null) note.updateContent(req.contentMd());
        if (req.status() != null) note.setStatus(req.status());
        if (req.tags() != null) note.setTags(req.tags());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Note note = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new IllegalArgumentException("forbidden");
        }
        repo.delete(note);
    }

    private NoteDtos.Response toResponse(Note n) {
        return new NoteDtos.Response(
                n.getId(),
                n.getTitle(),
                n.getContentMd(),
                n.getContentText(),
                n.getStatus(),
                n.getTags()
        );
    }
}
