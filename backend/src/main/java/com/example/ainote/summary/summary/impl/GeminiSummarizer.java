package com.example.ainote.summary.summary.impl;

import com.example.ainote.summary.summary.PromptTemplate;
import com.example.ainote.summary.summary.Summarizer;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini REST API 기반 Summarizer
 * - 기본 엔드포인트: https://generativelanguage.googleapis.com
 * - URL: /v1beta/models/{model}:generateContent?key={API_KEY}
 */
public class GeminiSummarizer implements Summarizer {

    private final WebClient client;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final long timeoutMs;

    public GeminiSummarizer(
            String baseUrl,
            String apiKey,
            String model,
            double temperature,
            long timeoutMs
    ) {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://generativelanguage.googleapis.com";
        }
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Result summarize(String text, String style) {
        // 시스템 프롬프트 + 유저 프롬프트 합쳐서 하나의 텍스트로 보냄
        String prompt = PromptTemplate.system() + "\n\n" + PromptTemplate.user(style, text);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", 512
                )
        );

        Map<String, Object> resp = client.post()
                // 예: /v1beta/models/gemini-2.5-flash:generateContent?key=XXX
                .uri("/v1beta/models/" + model + ":generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .block(Duration.ofMillis(timeoutMs));

        String full = extractText(resp);
        int promptTokens = extractUsageInt(resp, "promptTokenCount", roughlyTokens(prompt));
        int outputTokens = extractUsageInt(resp, "candidatesTokenCount", roughlyTokens(full));
        String oneLine = firstLine(full, 80);

        return new Result(oneLine, full, promptTokens, outputTokens);
    }

    @Override
    public String modelId() {
        // Summary 에 모델 이름으로 저장됨
        return model;
    }

    // ---- 내부 헬퍼 메서드들 ----

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> resp) {
        try {
            if (resp == null) return "";

            // candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";

            Map<String, Object> first = candidates.get(0);
            Map<String, Object> content =
                    (Map<String, Object>) first.get("content");
            if (content == null) return "";

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "";

            Object text = parts.get(0).get("text");
            return text == null ? "" : text.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private int extractUsageInt(Map<String, Object> resp, String field, int defaultValue) {
        try {
            if (resp == null) return defaultValue;
            Map<String, Object> usage =
                    (Map<String, Object>) resp.get("usageMetadata");
            if (usage == null) return defaultValue;
            Object v = usage.get(field);
            if (v instanceof Number n) return n.intValue();
            if (v != null) return Integer.parseInt(v.toString());
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int roughlyTokens(String s) {
        return (s == null || s.isEmpty()) ? 0 : (s.length() / 4 + 1);
    }

    private String firstLine(String s, int n) {
        String f = (s == null) ? "" : s.lines().findFirst().orElse("").trim();
        return f.length() <= n ? f : f.substring(0, n);
    }
}
