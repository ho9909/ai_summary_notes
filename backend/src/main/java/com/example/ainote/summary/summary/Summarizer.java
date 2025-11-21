package com.example.ainote.summary.summary;

public interface Summarizer {
    Result summarize(String text, String style);

    default String modelId() { return "mock-1"; }

    record Result(String oneLine, String paragraph,
                  int tokensPrompt, int tokensOutput) {}
}
