package com.example.ainote.summary.config;

import com.example.ainote.summary.summary.MockSummarizer;
import com.example.ainote.summary.summary.Summarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummarizerConfig {

    private static final Logger log = LoggerFactory.getLogger(SummarizerConfig.class);

    @Value("${ai.provider:mock}")
    private String provider;

    @Bean("aiSummarizer")
    @ConditionalOnMissingBean(Summarizer.class) // 같은 타입 빈이 이미 있으면(예: @MockBean) 생성하지 않음
    public Summarizer summarizer() {
        return switch (provider) {
            case "openai" -> newInstanceOrMock("com.example.ainote.summary.summary.OpenAiSummarizer");
            case "ollama" -> newInstanceOrMock("com.example.ainote.summary.summary.OllamaSummarizer");
            default -> new MockSummarizer();
        };
    }

    private Summarizer newInstanceOrMock(String fqcn) {
        try {
            Class<?> cls = Class.forName(fqcn);
            Object obj = cls.getDeclaredConstructor().newInstance();
            return (Summarizer) obj;
        } catch (Throwable t) {
            log.warn("Summarizer class {} not found or failed to init. Falling back to Mock.", fqcn);
            return new MockSummarizer();
        }
    }
}
