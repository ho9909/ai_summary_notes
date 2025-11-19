package com.example.ainote.user.domain;

import jakarta.persistence.*;

@Entity @Table(name="users")
public class User {
    @Id
    private Long id;
    @Column(nullable=false, length=50)
    private String nickname;

    protected User() {}
    public User(Long id, String nickname){ this.id=id; this.nickname=nickname; }
    public Long getId(){ return id; }
    public String getNickname(){ return nickname; }
    public void setNickname(String nickname){ this.nickname = nickname; }
}
