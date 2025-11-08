package com.example.ainote.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(
        new Info()
            .title("AI Summary Notes (MVP-Lite)")
            .version("0.1.0")
            .description("X-USER-ID ?§Îçî Í∏∞Î∞ò???∏Ìä∏ CRUD + ?îÏïΩ(Mock) API")
    );
  }
}
