package com.example.ainote.common.config;

import com.example.ainote.summary.summary.MockSummarizer;
import com.example.ainote.summary.summary.Summarizer;
import com.example.ainote.summary.summary.impl.OllamaSummarizer;
import com.example.ainote.summary.summary.impl.OpenAiSummarizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SummarizerConfig {
  @Value("${ai.provider:mock}") private String provider;

  @Value("${ai.openai.api-key:}") private String openAiKey;
  @Value("${ai.openai.model:gpt-4o-mini}") private String openAiModel;
  @Value("${ai.openai.base-url:https://api.openai.com}") private String openAiBase;
  @Value("${ai.ollama.base-url:http://localhost:11434}") private String ollamaBase;
  @Value("${ai.ollama.model:llama3.1}") private String ollamaModel;
  @Value("${ai.temperature:0.2}") private double temperature;
  @Value("${ai.timeout-ms:20000}") private long timeoutMs;

  @Bean
  public Summarizer summarizer() {
    return switch (provider.toLowerCase()) {
      case "openai" -> (openAiKey == null || openAiKey.isBlank())
        ? new MockSummarizer()
        : new OpenAiSummarizer(openAiBase, openAiKey, openAiModel, temperature, timeoutMs);
      case "ollama" -> new OllamaSummarizer(ollamaBase, ollamaModel, temperature, timeoutMs);
      default -> new MockSummarizer();
    };
  }
}
