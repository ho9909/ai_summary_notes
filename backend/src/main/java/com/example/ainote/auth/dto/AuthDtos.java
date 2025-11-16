package com.example.ainote.auth.dto;

public class AuthDtos {
    public record LoginReq(Long userId, String nickname) {}
    public record LoginResp(Long userId, String nickname, String howToUseHeader) {}
}
