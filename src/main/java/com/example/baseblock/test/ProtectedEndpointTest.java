package com.example.baseblock.test;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/protected-test")
public class ProtectedEndpointTest {

    @GetMapping
    public String protectedEndpoint() {
        return "인증된 사용자만 볼 수 있는 메시지 입니다.";
    }

}
