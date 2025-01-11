package com.example.demo.controller;

import com.example.demo.annotation.Auth;
import com.example.demo.annotation.OwnerCheck;
import com.example.demo.annotation.UserCheck;
import com.example.demo.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {

    @UserCheck
    @PostMapping("/user-only")
    public ResponseEntity<String> userOnly(@Auth AuthUser authUser) {
        return ResponseEntity.ok("userOnly 정상 호출");
    }

    @OwnerCheck
    @PostMapping("/owner-only")
    public ResponseEntity<String> ownerOnly(@Auth AuthUser authUser) {
        return ResponseEntity.ok("ownerOnly 정상 호출");
    }
}
