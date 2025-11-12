package com.example.ainote.summary.summary.impl;

import com.example.ainote.summary.summary.Summarizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions 기반 Summarizer 구현.
 * - baseUrl: 예) https://api.openai.com
 * - apiKey: "Bearer " 접두사 없이 순수 키만
 * - model: 예) gpt-4o-mini
 */
public class OpenAiSummarizer implements Summarizer {

    private final WebClient web;
    private final String model;
    private final double temperature;
    private final long timeoutMs;

    public OpenAiSummarizer(String baseUrl,
                            String apiKey,
                            String model,
                            double temperature,
                            long timeoutMs) {
        this.web = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.model = model;
        this.temperature = temperature;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Result summarize(String text, String style) {
        String normalized = normalize(text);
        String styleSafe = (style == null || style.isBlank()) ? "brief" : style.toLowerCase();

        String system = """
            You are a helpful summarizer for note-taking.
            Return Korean if the input is Korean.
            Respond clearly and concisely.
            """;

        String user = (styleSafe.equals("detailed")
                ? "Make 3-5 bullet points focusing on key facts."
                : "Make a single short paragraph. The first sentence should be <= 80 chars.")
                + "\n\nCONTENT:\n" + normalized;

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                )
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = web.post()
                .uri("/v1/chat/completions")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500)).filter(ex -> true))
                .block();

        String content = extractChatContent(resp);
        String oneLine = firstLine(content, 80);

        int pt = roughlyTokens(normalized);
        int ot = roughlyTokens(content);
        return new Result(oneLine, content, pt, ot);
    }

    @SuppressWarnings("unchecked")
    private String extractChatContent(Map<String, Object> resp) {
        if (resp == null) return "";
        try {
            var choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return "";
            var message = (Map<String, Object>) choices.get(0).get("message");
            Object content = (message != null) ? message.get("content") : null;
            return content == null ? "" : content.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private int roughlyTokens(String s) {
        return (s == null || s.isEmpty()) ? 0 : (s.length() / 4 + 1);
    }

    private String firstLine(String s, int n) {
        String f = (s == null) ? "" : s.lines().findFirst().orElse("").trim();
        return f.length() <= n ? f : f.substring(0, n);
    }
}
