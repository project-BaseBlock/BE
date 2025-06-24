package com.example.baseblock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Webconfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer crosConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")  // 어떤 api에 적용할지
                        .allowedOrigins("http://localhost:3000") // 프론트 도메인
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true) //쿠키, 헤더 등 인증 정보 허용
                        .maxAge(3600);  // preflight 결과 캐시 시간 (초)
                /*
                | 설정                     | 의미                                             |
                | ------------------------ | -------------------------------------------------|
                | `addMapping("/api/**")`  | `/api/`로 시작하는 모든 요청에 대해 적용         |
                | `allowedOrigins(...)`    | React 앱 주소 허용 (도메인 or 포트)              |
                | `allowedMethods(...)`    | REST API 허용 메서드 (GET, POST 등)              |
                | `allowCredentials(true)` | JWT, 쿠키 등 인증 정보 사용 허용                 |
                | `maxAge(3600)`           | 브라우저가 CORS preflight 요청을 1시간 동안 캐시 |
                */
            }
        };
    }

}
