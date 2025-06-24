package com.example.baseblock.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SecureTestController {

    @GetMapping("/secure-test")
    public String secureTest() {
        return "인증 성공 토큰이 작동합니다.";
    }
    // /api/secure-test 경로는 토큰 없이는 막아야 하니까, SecureConfig에 보호 경로 등록해야함

}
