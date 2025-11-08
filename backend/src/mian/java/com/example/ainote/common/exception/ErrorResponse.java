package com.example.ainote.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse (    Instance timestamp,String path, Stirng error, String code, List<String> messages)
{
    public static ErrorResponse of(String path, String error, String code, List<String> messages) {
        return new ErrorResponse(Instant.now(), path, error, code, messages);
    }
}


