// src/main/java/com/example/ainote/common/config/OpenApiConfig.java
package com.example.ainote.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.IntegerSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info()
        .title("AI Summary Notes (MVP-Lite)")
        .version("0.1.0")
        .description("X-USER-ID 헤더 기반의 노트 CRUD + 요약(Mock) API"));
  }

  // 모든 엔드포인트에 X-USER-ID 헤더 자동 추가
  @Bean
  public OpenApiCustomizer addGlobalUserIdHeader() {
    return openApi -> openApi.getPaths().values()
        .forEach(path -> path.readOperations().forEach(op -> op.addParametersItem(new Parameter()
            .in("header")
            .required(true)
            .name("X-USER-ID")
            .description("Mock user id (e.g., 1)")
            .schema(new IntegerSchema()))));
  }
}
