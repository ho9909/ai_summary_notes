package com.example.ainote.note;

import com.example.ainote.common.web.UserId;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import com.example.ainote.note.dto.NoteDtos;
import com.example.ainote.note.dto.NoteDtos.Response;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService svc;

    public NoteController(NoteService svc) {
        this.svc = svc;
    }

    /** 목록 조회 (검색어 선택, 페이징) */
    @GetMapping
    public Page<NoteDtos.Response> list(
            @UserId Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return svc.list(userId, query, PageRequest.of(page, size));
    }

    /** 노트 생성 */
    @PostMapping
    public Long create(
            @UserId Long userId,
            @Valid @RequestBody NoteDtos.Create req
    ) {
        return svc.create(userId, req);
    }

    /** 노트 상세 */
    @GetMapping("/{id}")
    public Response detail(
            @UserId Long userId,
            @PathVariable Long id
    ) {
        return svc.detail(userId, id);
    }

    /** 노트 수정(부분 업데이트) */
    @PatchMapping("/{id}")
    public void update(
            @UserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody NoteDtos.Update req
    ) {
        svc.update(userId, id, req);
    }

    /** 노트 삭제 */
    @DeleteMapping("/{id}")
    public void delete(
            @UserId Long userId,
            @PathVariable Long id
    ) {
        svc.delete(userId, id);
    }
}
