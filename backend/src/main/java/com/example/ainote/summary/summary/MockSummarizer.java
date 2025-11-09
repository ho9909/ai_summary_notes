package com.example.ainote.summary.summary;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class MockSummarizer implements Summarizer {

    @Override
    public Result summarize(String text, String style) {
        if (text == null) text = "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        int promptTokens = normalized.length() / 4 + 1;

        if (style == null || style.isBlank()) style = "brief";
        style = style.toLowerCase();

        if (style.equals("detailed")) {
            String[] sentences = normalized.split("[\\.\\!\\?\\n]+");
            String bullets = Arrays.stream(sentences)
                    .limit(5)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> "- " + s)
                    .collect(Collectors.joining("\n"));

            int outputTokens = bullets.length() / 4 + 1;
            String oneLine = firstLine(bullets, 80);
            return new Result(oneLine, bullets, promptTokens, outputTokens);
        }

        // brief 스타일
        String oneLine = truncate(normalized, 80);
        int outputTokens = oneLine.length() / 4 + 1;
        return new Result(oneLine, oneLine, promptTokens, outputTokens);
    }

    private String truncate(String s, int len) {
        return s.length() <= len ? s : s.substring(0, len);
    }

    private String firstLine(String s, int len) {
        String first = s.lines().findFirst().orElse("");
        if (first.startsWith("- ")) first = first.substring(2);
        return truncate(first, len);
    }
}
