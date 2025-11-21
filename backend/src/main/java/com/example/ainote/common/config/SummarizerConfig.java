package com.example.ainote.common.config;

import com.example.ainote.summary.summary.MockSummarizer;
import com.example.ainote.summary.summary.Summarizer;
import com.example.ainote.summary.summary.impl.GeminiSummarizer;
import com.example.ainote.summary.summary.impl.OllamaSummarizer;
import com.example.ainote.summary.summary.impl.OpenAiSummarizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummarizerConfig {

    @Value("${ai.provider:mock}")
    private String provider;

    // OpenAI 설정 (이미 가지고 있던 값 그대로 유지)
    @Value("${ai.openai.api-key:}")
    private String openAiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${ai.openai.base:https://api.openai.com}")
    private String openAiBase;

    // Gemini 설정 (새로 추가)
    @Value("${ai.gemini.api-key:}")
    private String geminiKey;

    // 모델은 2.x 계열을 추천 (예: gemini-2.5-flash)
    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.base:https://generativelanguage.googleapis.com}")
    private String geminiBase;

    // Ollama
    @Value("${ai.ollama.base:http://localhost:11434}")
    private String ollamaBase;

    @Value("${ai.ollama.model:llama3.1}")
    private String ollamaModel;

    // 공통 옵션
    @Value("${ai.temperature:0.2}")
    private double temperature;

    @Value("${ai.timeout-ms:20000}")
    private long timeoutMs;

    @Bean
    public Summarizer summarizer() {
        return switch (provider.toLowerCase()) {
            case "openai" -> (openAiKey == null || openAiKey.isBlank())
                    ? new MockSummarizer()
                    : new OpenAiSummarizer(openAiBase, openAiKey, openAiModel, temperature, timeoutMs);

            case "gemini" -> (geminiKey == null || geminiKey.isBlank())
                    ? new MockSummarizer()
                    : new GeminiSummarizer(geminiBase, geminiKey, geminiModel, temperature, timeoutMs);

            case "ollama" -> new OllamaSummarizer(ollamaBase, ollamaModel, temperature, timeoutMs);

            default -> new MockSummarizer();
        };
    }
}
