package com.example.ainote.summary.repo;

import com.example.ainote.summary.domain.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findByNoteIdOrderByCreatedAtDesc(Long noteId);
}
