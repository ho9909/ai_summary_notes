// src/main/java/com/example/ainote/common/config/WebConfig.java
package com.example.ainote.common.config;

import com.example.ainote.common.web.UserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final UserIdArgumentResolver resolver;
  public WebConfig(UserIdArgumentResolver resolver) { this.resolver = resolver; }
  @Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(resolver);
  }
}
