package com.example.ainote.note.repo;

import com.example.ainote.note.domain.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // 목록: 사용자별 최신순
    Page<Note> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 검색: 제목 like, 사용자별 최신순
    Page<Note> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long userId, String title, Pageable pageable
    );
}
