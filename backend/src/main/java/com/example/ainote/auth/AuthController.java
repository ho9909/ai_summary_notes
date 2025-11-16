package com.example.ainote.auth;

import com.example.ainote.auth.dto.AuthDtos;
import com.example.ainote.user.domain.User;
import com.example.ainote.user.repo.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    public AuthController(UserRepository users){ this.users = users; }

    /**
     * 아주 단순한 로그인: userId가 있으면 그걸 사용, 없으면 새로 발급(닉네임 필요).
     * 프론트는 응답의 userId를 localStorage 등에 저장하고, 모든 요청 헤더에 X-USER-ID로 실어 보낸다.
     */
    @PostMapping("/login")
    @Transactional
    public AuthDtos.LoginResp login(@RequestBody AuthDtos.LoginReq req){
        User u;
        if (req.userId() != null) {
            u = users.findById(req.userId()).orElseThrow(() -> new IllegalArgumentException("user not found"));
        } else {
            if (req.nickname()==null || req.nickname().isBlank()) throw new IllegalArgumentException("nickname required");
            // 간단 발급: max(id)+1
            long nextId = users.findAll().stream().mapToLong(User::getId).max().orElse(1L) + 1L;
            u = new User(nextId, req.nickname());
            users.save(u);
        }
        return new AuthDtos.LoginResp(u.getId(), u.getNickname(), "Send header: X-USER-ID: "+u.getId());
    }
}
