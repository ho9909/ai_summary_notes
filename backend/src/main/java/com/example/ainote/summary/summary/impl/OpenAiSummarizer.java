package com.example.ainote.summary.summary.impl;

import com.example.ainote.summary.summary.PromptTemplate;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiSummarizer implements Summarizer {

    private final WebClient web;
    private final String model;
    private final double temperature;
    private final long timeoutMs;

    public OpenAiSummarizer(String baseUrl, String apiKey, String model, double temperature, long timeoutMs) {
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
    public String modelId() { return model; }

    @Override
    public Result summarize(String text, String style) {
        String sys = PromptTemplate.system();
        String user = PromptTemplate.user(style, text);

        Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system", "content", sys),
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
        int pt = roughlyTokens(user);    // 입력 토큰(대략)
        int ot = roughlyTokens(content); // 출력 토큰(대략)
        return new Result(oneLine, content, pt, ot);
    }

    @SuppressWarnings("unchecked")
    private String extractChatContent(Map<String, Object> resp) {
        if (resp == null) return "";
        try {
            var choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return "";
            var msg = (Map<String, Object>) choices.get(0).get("message");
            Object c = (msg != null) ? msg.get("content") : null;
            return c == null ? "" : c.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private int roughlyTokens(String s) { return (s == null || s.isEmpty()) ? 0 : (s.length() / 4 + 1); }
    private String firstLine(String s, int n) {
        String f = (s == null) ? "" : s.lines().findFirst().orElse("").trim();
        return f.length() <= n ? f : f.substring(0, n);
    }
}
