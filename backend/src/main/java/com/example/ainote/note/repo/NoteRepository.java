package com.example.ainote.note.repo;

import com.example.ainote.note.domain.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Note> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String title, Pageable pageable);
    Page<Note> findByUserIdAndTagsContainingIgnoreCaseOrderByCreatedAtDesc(Long userId, String tag, Pageable pageable);
}
