// src/main/java/com/example/ainote/common/web/UserIdArgumentResolver.java
package com.example.ainote.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(UserId.class)
        && parameter.getParameterType().equals(Long.class);
  }
  @Override
  public Object resolveArgument(MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    String h = webRequest.getHeader("X-USER-ID");
    if (h == null || !h.matches("\\d+")) throw new IllegalArgumentException("missing X-USER-ID");
    return Long.parseLong(h);
  }
}
