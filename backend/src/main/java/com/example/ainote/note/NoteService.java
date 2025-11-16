package com.example.ainote.note;

import com.example.ainote.note.dto.NoteDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {

    Page<NoteDtos.Response> list(Long userId, String query, Pageable pageable);

    Page<NoteDtos.Response> listByTag(Long userId, String tag, Pageable pageable);

    // ✅ query + tag 동시 필터
    Page<NoteDtos.Response> listByQueryAndTag(Long userId, String query, String tag, Pageable pageable);

    Long create(Long userId, NoteDtos.Create req);

    NoteDtos.Response detail(Long userId, Long id);

    void update(Long userId, Long id, NoteDtos.Update req);

    void delete(Long userId, Long id);
}
