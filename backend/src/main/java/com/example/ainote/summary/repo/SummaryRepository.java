package com.example.ainote.summary.repo;

import com.example.ainote.summary.domain.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    // 최신 1개
    Optional<Summary> findTopByNoteIdOrderByCreatedAtDesc(Long noteId);

    // 목록(페이징)
    Page<Summary> findByNoteIdOrderByCreatedAtDesc(Long noteId, Pageable pageable);
}
