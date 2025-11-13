package com.example.ainote.summary.summary.impl;

import com.example.ainote.summary.summary.PromptTemplate;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

public class OllamaSummarizer implements Summarizer {

    private final WebClient web;
    private final String model;
    private final double temperature;
    private final long timeoutMs;

    public OllamaSummarizer(String baseUrl, String model, double temperature, long timeoutMs) {
        this.web = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
        String prompt = PromptTemplate.user(style, text);

        Map<String, Object> payload = Map.of(
                "model", model,
                "prompt", prompt,
                "temperature", temperature,
                "stream", false
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = web.post()
                .uri("/api/generate")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500)).filter(ex -> true))
                .block();

        String content = (resp != null && resp.get("response") != null) ? resp.get("response").toString().trim() : "";
        String oneLine = firstLine(content, 80);
        int pt = roughlyTokens(prompt);
        int ot = roughlyTokens(content);
        return new Result(oneLine, content, pt, ot);
    }

    private int roughlyTokens(String s) { return (s == null || s.isEmpty()) ? 0 : (s.length() / 4 + 1); }
    private String firstLine(String s, int n) {
        String f = (s == null) ? "" : s.lines().findFirst().orElse("").trim();
        return f.length() <= n ? f : s.substring(0, n);
    }
}
