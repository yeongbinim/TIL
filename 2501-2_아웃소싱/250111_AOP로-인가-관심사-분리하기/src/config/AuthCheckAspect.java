package com.example.demo.config;

import com.example.demo.dto.AuthUser;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

//두 개의 어드바이저 적용
@Aspect
@Component
@RequiredArgsConstructor
public class AuthCheckAspect {
    @Before("@annotation(com.example.demo.annotation.OwnerCheck)")
    public void ownerCheck(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
            .filter(AuthUser.class::isInstance)
            .map(AuthUser.class::cast)
            .filter(AuthUser::isOwner)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("OWNER 아님"));
    }

    @Before("@annotation(com.example.demo.annotation.UserCheck) && args(authUser, ..)")
    public void userCheck(AuthUser authUser) {
        if (!authUser.isUser()) {
            throw new RuntimeException("USER 아님");
        }
    }
}
