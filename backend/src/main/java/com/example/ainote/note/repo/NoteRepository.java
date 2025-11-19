package com.example.ainote.note.repo;

import com.example.ainote.note.domain.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Note> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long userId, String title, Pageable pageable);

    // (기존 containsIgnoreCase는 부분매칭 문제) -> 정확 매칭으로 교체
    @Query("""
           select n
             from Note n
            where n.userId = :userId
              and lower(concat(',', coalesce(n.tags, ''), ',')) like lower(concat('%,', :tag, ',%'))
            order by n.createdAt desc
           """)
    Page<Note> findByUserIdAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("""
           select n
             from Note n
            where n.userId = :userId
              and lower(n.title) like lower(concat('%', :title, '%'))
              and lower(concat(',', coalesce(n.tags, ''), ',')) like lower(concat('%,', :tag, ',%'))
            order by n.createdAt desc
           """)
    Page<Note> findByUserIdAndTitleLikeAndTagsCsvContainsIgnoreCaseOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("tag") String tag,
            Pageable pageable);
}
