package com.example.ainote.note;

import com.example.ainote.note.dto.NoteDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService svc;

    public NoteController(NoteService svc) {
        this.svc = svc;
    }

    private Long uid(HttpServletRequest req) {
        String h = req.getHeader("X-USER-ID");
        if (h == null || !h.matches("\\d+"))
            throw new IllegalArgumentException("missing X-USER-ID");
        return Long.parseLong(h);
    }

    @PostMapping
    public Long create(@Valid @RequestBody NoteDtos.Create req, HttpServletRequest r) {
        return svc.create(uid(r), req);
    }

    @GetMapping("/{id}")
    public NoteDtos.Response get(@PathVariable Long id, HttpServletRequest r) {
        return svc.get(uid(r), id);
    }

    @GetMapping
    public Page<NoteDtos.Response> list(@RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest r) {
        return svc.list(uid(r), query, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody NoteDtos.Update req, HttpServletRequest r) {
        svc.update(uid(r), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest r) {
        svc.delete(uid(r), id);
    }
}
