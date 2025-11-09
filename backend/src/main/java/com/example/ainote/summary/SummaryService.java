package com.example.ainote.summary;

public interface SummaryService {
    Resp summarize(Long userId, Long noteId, Req req);

    record Req(String style) {}
    record Resp(String oneLine, String paragraph,
                String model, String style,
                int tokensPrompt, int tokensOutput) {}
}
