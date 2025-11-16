package com.example.ainote.note;

import com.example.ainote.common.web.UserId;
import com.example.ainote.note.dto.NoteDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService svc;
    public NoteController(NoteService svc) { this.svc = svc; }

    @GetMapping
    public Page<NoteDtos.Response> list(
            @UserId Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var p = PageRequest.of(page, size);
        return (tag != null && !tag.isBlank()) ? svc.listByTag(userId, tag, p) : svc.list(userId, query, p);
    }

    @PostMapping
    public Long create(@UserId Long userId, @Valid @RequestBody NoteDtos.Create req) {
        return svc.create(userId, req);
    }

    @GetMapping("/{id}")
    public NoteDtos.Response detail(@UserId Long userId, @PathVariable Long id) {
        return svc.detail(userId, id);
    }

    @PatchMapping("/{id}")
    public void update(@UserId Long userId, @PathVariable Long id, @Valid @RequestBody NoteDtos.Update req) {
        svc.update(userId, id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@UserId Long userId, @PathVariable Long id) {
        svc.delete(userId, id);
    }
}
