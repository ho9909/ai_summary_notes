package com.example.ainote.common.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingRequestHeaderException; // ✅ 필수
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class) 
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String value = webRequest.getHeader("X-USER-ID");

        // [핵심] 값이 없으면 400 에러를 내는 스프링 "공식" 예외를 던집니다.
        if (value == null) {
            throw new MissingRequestHeaderException("X-USER-ID", parameter);
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 숫자가 아니어도 400 에러 처리
            throw new MissingRequestHeaderException("X-USER-ID", parameter);
        }
    }
}