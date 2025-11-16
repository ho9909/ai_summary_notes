package com.example.ainote.user.domain;

import jakarta.persistence.*;

@Entity @Table(name="users")
public class User {
    @Id
    private Long id;             // 프리코스에선 간단히 지정(1 고정) 또는 시퀀스 가능
    @Column(nullable=false, length=50)
    private String nickname;

    protected User() {}
    public User(Long id, String nickname){ this.id=id; this.nickname=nickname; }
    public Long getId(){ return id; }
    public String getNickname(){ return nickname; }
    public void setNickname(String nickname){ this.nickname = nickname; }
}
