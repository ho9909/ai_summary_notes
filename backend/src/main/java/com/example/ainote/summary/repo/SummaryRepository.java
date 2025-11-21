package com.example.ainote.summary.repo;

import com.example.ainote.summary.domain.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Page<Summary> findByNoteIdOrderByCreatedAtDesc(Long noteId, Pageable pageable);
    Optional<Summary> findTopByNoteIdOrderByCreatedAtDesc(Long noteId);
    long countByNoteId(Long noteId);
}