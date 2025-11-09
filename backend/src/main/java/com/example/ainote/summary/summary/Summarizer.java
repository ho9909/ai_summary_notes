package com.example.ainote.summary.summary;

public interface Summarizer {
    Result summarize(String text, String style);

    record Result(String oneLine, String paragraph,
                  int tokensPrompt, int tokensOutput) {}
}
