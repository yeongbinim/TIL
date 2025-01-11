package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private String email;
    private UserRole userRole;

    public boolean isOwner() {
        return UserRole.OWNER.equals(userRole);
    }

    public boolean isUser() {
        return UserRole.USER.equals(userRole);
    }
}
