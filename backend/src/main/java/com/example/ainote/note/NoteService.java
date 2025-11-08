package com.example.ainote.note;

import com.example.ainote.note.dto.NoteDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {
    Long create(Long userId, NoteDtos.Create req);

    NoteDtos.Response get(Long userId, Long id);

    Page<NoteDtos.Response> list(Long userId, String query, Pageable pageable);

    void update(Long userId, Long id, NoteDtos.Update req);

    void delete(Long userId, Long id);
}
