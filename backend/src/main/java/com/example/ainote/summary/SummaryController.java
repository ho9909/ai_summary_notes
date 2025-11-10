package com.example.ainote.summary;

import com.example.ainote.common.web.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes/{id}/summary")
public class SummaryController {
    private final SummaryService svc;

    public SummaryController(SummaryService svc) {
        this.svc = svc;
    }

    @PostMapping
    public SummaryService.Resp create(
            @UserId Long userId,
            @PathVariable("id") Long noteId,
            @RequestBody(required = false) SummaryService.Req req
    ) {
        return svc.summarize(userId, noteId, req);
    }

    @GetMapping
    public ResponseEntity<SummaryService.LatestResp> latest(
            @UserId Long userId,
            @PathVariable("id") Long noteId
    ) {
        var body = svc.latest(userId, noteId);
        return (body == null) ? ResponseEntity.noContent().build() : ResponseEntity.ok(body);
    }

    @GetMapping("/list")
    public Page<SummaryService.Item> list(
            @UserId Long userId,
            @PathVariable("id") Long noteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return svc.list(userId, noteId, PageRequest.of(page, size));
    }
}
