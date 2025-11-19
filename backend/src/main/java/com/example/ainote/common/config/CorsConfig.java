package com.example.ainote.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${app.cors.allowed-origins:*}")
  private String allowedOrigins; // 쉼표구분

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = allowedOrigins.split("\\s*,\\s*");
    registry.addMapping("/api/**")
        .allowedOriginPatterns(origins)
        .allowedMethods("GET","POST","PATCH","DELETE","OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("*")
        .allowCredentials(false)
        .maxAge(3600);
  }
}
