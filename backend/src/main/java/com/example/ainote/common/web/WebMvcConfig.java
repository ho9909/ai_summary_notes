package com.example.ainote.common.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserIdArgumentResolver userIdArgumentResolver;

    // @Component로 등록된 Resolver를 스프링이 주입해줍니다.
    public WebMvcConfig(UserIdArgumentResolver userIdArgumentResolver) {
        this.userIdArgumentResolver = userIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // [핵심] 리스트에 추가를 안 하면, 스프링은 Resolver의 존재를 모릅니다!
        resolvers.add(userIdArgumentResolver);
    }
}