package com.example.ainote.user;

import com.example.ainote.common.web.UserId;
import com.example.ainote.user.domain.User;
import com.example.ainote.user.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;
    public UserController(UserRepository repo){ this.repo = repo; }

    /** 현재 사용자 프로필 */
    @GetMapping("/me")
    public User me(@UserId Long userId){
        return repo.findById(userId).orElse(new User(userId, "guest"));
    }

    /** 단일 조회(디버그용) */
    @GetMapping("/{id}")
    public User get(@PathVariable Long id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }
}
