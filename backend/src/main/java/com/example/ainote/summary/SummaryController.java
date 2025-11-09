package com.example.ainote.summary;

import com.example.ainote.common.web.UserId;
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
}
